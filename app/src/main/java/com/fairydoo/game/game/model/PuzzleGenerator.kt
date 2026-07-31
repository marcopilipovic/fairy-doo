package com.fairydoo.game.game.model

import kotlin.math.abs
import kotlin.random.Random

/**
 * Erzeugt Rätsel mit **eindeutiger** Lösung.
 *
 * Eindeutigkeit ist kein Luxus: Ohne sie könnte der Hinweis-Feenstaub ein Feld
 * aufdecken, das zu einer anderen gültigen Lösung gehört als der, die der
 * Spieler gerade baut — der Hinweis wäre dann schlicht falsch.
 *
 * Der Weg dorthin: erst eine zufällige Lösung würfeln, dann die Zonen so um die
 * Lösungsfelder herum wachsen lassen, dass jede Zone genau eine Fee enthält.
 * Anschließend prüft der Solver, ob das Ergebnis wirklich nur diese eine Lösung
 * zulässt; sonst wird neu gewürfelt.
 */
object PuzzleGenerator {

    /** Kleiner geht es nicht: Für 3×3 existiert keine gültige Feen-Anordnung. */
    const val MIN_SIZE = 4

    /**
     * @param size Kantenlänge des Gitters, mindestens [MIN_SIZE].
     * @param random Zufallsquelle — mit festem Seed werden Rätsel reproduzierbar,
     *   was Tests und das Nachstellen von Fehlerberichten erlaubt.
     */
    fun generate(size: Int, random: Random = Random.Default): Puzzle {
        require(size >= MIN_SIZE) { "Gitter muss mindestens ${MIN_SIZE}x$MIN_SIZE sein, war $size" }

        var uniqueOnly: Puzzle? = null
        var anyPuzzle: Puzzle? = null

        repeat(MAX_ATTEMPTS) {
            val columns = randomSolution(size, random) ?: return@repeat
            val solution = columns.mapIndexed { row, col -> Pos(row, col) }.toSet()
            val grown = growRegions(size, solution, random).toIntArray()

            // Zufällig gewachsene Zonen sind ab etwa 7×7 fast nie eindeutig;
            // die Eindeutigkeit wird deshalb gezielt hergestellt.
            enforceUniqueness(size, grown, solution, random)

            val regions = grown.toList()
            val puzzle = Puzzle(size = size, regions = regions, solution = solution)

            val isUnique = countSolutions(puzzle, limit = 2) == 1
            val hasNoTinyRegion = regions.groupingBy { it }.eachCount()
                .values.min() >= MIN_REGION_CELLS

            // Das Beste ist beides. Eindeutigkeit wiegt aber schwerer als die
            // Zonengröße: Bei mehreren Lösungen zeigt der Feenstaub womöglich
            // ein Feld, das zu einer *anderen* Lösung gehört als der, die der
            // Spieler gerade baut. Eine kleine Zone ist dagegen nur fade.
            if (isUnique && hasNoTinyRegion) return puzzle
            if (isUnique && uniqueOnly == null) uniqueOnly = puzzle
            if (anyPuzzle == null) anyPuzzle = puzzle
        }

        return uniqueOnly
            ?: anyPuzzle
            ?: error("Kein lösbares Rätsel für Größe $size gefunden")
    }

    /**
     * Würfelt eine gültige Feen-Anordnung: je Zeile eine Spalte, keine Spalte
     * doppelt, benachbarte Zeilen mindestens zwei Spalten auseinander (sonst
     * würden sich die Feen diagonal berühren).
     *
     * @return Spaltenindex je Zeile, oder null wenn dieser Versuch scheitert.
     */
    private fun randomSolution(size: Int, random: Random): List<Int>? {
        val columns = IntArray(size) { -1 }
        val used = BooleanArray(size)

        fun place(row: Int): Boolean {
            if (row == size) return true

            for (col in (0 until size).shuffled(random)) {
                if (used[col]) continue
                if (row > 0 && abs(col - columns[row - 1]) < 2) continue

                columns[row] = col
                used[col] = true
                if (place(row + 1)) return true
                used[col] = false
                columns[row] = -1
            }
            return false
        }

        return if (place(0)) columns.toList() else null
    }

    /**
     * Lässt aus jedem Lösungsfeld eine Zone wachsen, bis das Gitter gefüllt ist.
     *
     * Weil jede Zone von genau einem Lösungsfeld ausgeht, enthält sie am Ende
     * genau eine Fee — die Zonen-Regel ist damit konstruktionsbedingt erfüllt.
     *
     * Das Wachstum läuft in zwei Phasen, und beide werden gebraucht:
     *
     * 1. **Reihum**, bis jede Zone [MIN_REGION_CELLS] Felder hat. Ohne diese
     *    Phase gewinnt die Zone mit dem längsten Rand immer weiter Fläche, und
     *    es entstehen Ein-Feld-Zonen — die ihre Fee sofort verraten.
     * 2. **Frei**, bis das Gitter voll ist, gedeckelt auf [maxRegionCells].
     *    Diese Phase erzeugt die langgezogenen, ineinandergreifenden Zonen.
     *    Bliebe es beim gleichmäßigen Wachsen, wären die Zonen kompakte
     *    Klötze — und kompakte Zonen schränken die Feen kaum ein: Ab 6×6 hatte
     *    dann praktisch jedes Brett dutzende Lösungen statt einer.
     */
    private fun growRegions(size: Int, solution: Set<Pos>, random: Random): List<Int> {
        val regions = IntArray(size * size) { UNASSIGNED }

        val seeds = solution.sortedWith(compareBy({ it.row }, { it.col }))
        seeds.forEachIndexed { region, pos ->
            regions[pos.row * size + pos.col] = region
        }

        // Je Zone die eigenen Randkandidaten: noch freie Nachbarfelder.
        val frontiers = seeds.map { seed ->
            orthogonalNeighbours(seed, size)
                .filter { regions[it.row * size + it.col] == UNASSIGNED }
                .toMutableList()
        }
        val counts = IntArray(seeds.size) { 1 }

        /** Nimmt ein freies Randfeld der Zone in Besitz, oder null. */
        fun claim(region: Int): Pos? {
            val frontier = frontiers[region]
            while (frontier.isNotEmpty()) {
                val candidate = frontier.removeAt(random.nextInt(frontier.size))
                if (regions[candidate.row * size + candidate.col] != UNASSIGNED) continue

                regions[candidate.row * size + candidate.col] = region
                counts[region]++
                frontier += orthogonalNeighbours(candidate, size)
                    .filter { regions[it.row * size + it.col] == UNASSIGNED }
                return candidate
            }
            return null
        }

        var remaining = size * size - seeds.size

        // Erst eine Runde reihum, damit keine Zone gleich zu Beginn abgeschnitten
        // wird; den Rest übernimmt das freie Wachstum unten.
        for (region in frontiers.indices) {
            if (remaining == 0) break
            if (claim(region) != null) remaining--
        }

        // Freies Wachstum über einen gemeinsamen Topf aller Randfelder.
        //
        // Entscheidend ist, dass hier *das Feld* gezogen wird und nicht die
        // Zone: Dadurch wächst eine Zone umso wahrscheinlicher, je länger ihr
        // Rand schon ist. Genau diese Ungleichheit erzeugt die langgezogenen,
        // verschränkten Zonen, die das Rätsel eindeutig machen. Zöge man
        // stattdessen reihum die Zone, blieben die Zonen kompakt — und ab 7×7
        // hatte dann kein einziges Brett mehr eine eindeutige Lösung.
        val pool = mutableListOf<Pair<Pos, Int>>()
        frontiers.forEachIndexed { region, frontier ->
            frontier.forEach { pool += it to region }
            frontier.clear()
        }

        while (remaining > 0 && pool.isNotEmpty()) {
            val (pos, region) = pool.removeAt(random.nextInt(pool.size))
            val cell = pos.row * size + pos.col
            if (regions[cell] != UNASSIGNED) continue

            regions[cell] = region
            counts[region]++
            remaining--

            orthogonalNeighbours(pos, size)
                .filter { regions[it.row * size + it.col] == UNASSIGNED }
                .forEach { pool += it to region }
        }

        // Kann auftreten, wenn ein Feld von keiner Zone erreicht wurde:
        // der nächstbesten benachbarten Zone zuschlagen.
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = row * size + col
                if (regions[cell] != UNASSIGNED) continue
                regions[cell] = orthogonalNeighbours(Pos(row, col), size)
                    .map { regions[it.row * size + it.col] }
                    .firstOrNull { it != UNASSIGNED }
                    ?: 0
            }
        }

        return regions.toList()
    }

    /**
     * Schließt alternative Lösungen aus, bis nur noch die gewollte übrig ist.
     *
     * Vorgehen: eine Alternativlösung A suchen und ein Feld, das in A eine Fee
     * trägt, in die Zone eines anderen A-Feldes umhängen. Dann stünden in A
     * zwei Feen in derselben Zone — A ist ungültig. Umgehängt werden nur Felder,
     * die in der gewollten Lösung *keine* Fee tragen; die bleibt also gültig.
     *
     * Jeder Durchgang tilgt mindestens eine Alternative, deshalb terminiert das
     * Verfahren. Arbeitet direkt auf [regions].
     */
    private fun enforceUniqueness(
        size: Int,
        regions: IntArray,
        solution: Set<Pos>,
        random: Random,
    ) {
        repeat(MAX_REFINEMENTS) {
            val alternative = findAlternativeSolution(size, regions, solution) ?: return

            val movable = alternative.filter { it !in solution }.shuffled(random)
            for (cell in movable) {
                val currentRegion = regions[cell.row * size + cell.col]

                // Zielzone: die eines anderen Feldes aus A, die an `cell` grenzt.
                val target = alternative
                    .asSequence()
                    .filter { it != cell }
                    .map { regions[it.row * size + it.col] }
                    .filter { it != currentRegion }
                    .filter { candidate ->
                        orthogonalNeighbours(cell, size)
                            .any { regions[it.row * size + it.col] == candidate }
                    }
                    .firstOrNull()
                    ?: continue

                // Die abgebende Zone darf nicht in zwei Teile zerfallen.
                if (!staysConnectedWithout(size, regions, currentRegion, cell)) continue

                regions[cell.row * size + cell.col] = target
                return@repeat
            }

            // Keine Alternative ließ sich auflösen — der Aufrufer verwirft das
            // Rätsel anhand der anschließenden Eindeutigkeitsprüfung.
            return
        }
    }

    /** Erste Lösung, die von [solution] abweicht — oder null, wenn es keine gibt. */
    private fun findAlternativeSolution(
        size: Int,
        regions: IntArray,
        solution: Set<Pos>,
    ): Set<Pos>? {
        val usedColumns = BooleanArray(size)
        val usedRegions = BooleanArray(size)
        val chosen = IntArray(size) { -1 }
        var found: Set<Pos>? = null

        fun search(row: Int, previousColumn: Int) {
            if (found != null) return
            if (row == size) {
                val candidate = (0 until size).map { Pos(it, chosen[it]) }.toSet()
                if (candidate != solution) found = candidate
                return
            }

            for (col in 0 until size) {
                if (usedColumns[col]) continue
                if (row > 0 && abs(col - previousColumn) < 2) continue

                val region = regions[row * size + col]
                if (usedRegions[region]) continue

                usedColumns[col] = true
                usedRegions[region] = true
                chosen[row] = col
                search(row + 1, col)
                usedColumns[col] = false
                usedRegions[region] = false
                chosen[row] = -1

                if (found != null) return
            }
        }

        search(0, -1)
        return found
    }

    /** Bleibt [region] zusammenhängend, wenn [removed] herausgenommen wird? */
    private fun staysConnectedWithout(
        size: Int,
        regions: IntArray,
        region: Int,
        removed: Pos,
    ): Boolean {
        val cells = mutableSetOf<Pos>()
        for (row in 0 until size) {
            for (col in 0 until size) {
                val pos = Pos(row, col)
                if (pos != removed && regions[row * size + col] == region) cells += pos
            }
        }
        if (cells.isEmpty()) return false

        val start = cells.first()
        val seen = mutableSetOf(start)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (neighbour in orthogonalNeighbours(current, size)) {
                if (neighbour in cells && seen.add(neighbour)) queue += neighbour
            }
        }
        return seen.size == cells.size
    }

    private fun orthogonalNeighbours(pos: Pos, size: Int): List<Pos> = listOfNotNull(
        Pos(pos.row - 1, pos.col).takeIf { it.row >= 0 },
        Pos(pos.row + 1, pos.col).takeIf { it.row < size },
        Pos(pos.row, pos.col - 1).takeIf { it.col >= 0 },
        Pos(pos.row, pos.col + 1).takeIf { it.col < size },
    )

    /**
     * Zählt die Lösungen, höchstens bis [limit].
     *
     * Zeile für Zeile, weil je Zeile genau eine Fee steht: Dadurch reduziert
     * sich die Berührungsprüfung auf den Abstand zur Fee der Vorzeile.
     */
    fun countSolutions(puzzle: Puzzle, limit: Int = 2): Int {
        val size = puzzle.size
        val usedColumns = BooleanArray(size)
        val usedRegions = BooleanArray(size)
        var found = 0

        fun search(row: Int, previousColumn: Int) {
            if (found >= limit) return
            if (row == size) {
                found++
                return
            }

            for (col in 0 until size) {
                if (usedColumns[col]) continue
                if (row > 0 && abs(col - previousColumn) < 2) continue

                val region = puzzle.regions[row * size + col]
                if (usedRegions[region]) continue

                usedColumns[col] = true
                usedRegions[region] = true
                search(row + 1, col)
                usedColumns[col] = false
                usedRegions[region] = false

                if (found >= limit) return
            }
        }

        search(0, -1)
        return found
    }

    /** Kleinste Zone; alles darunter verrät die Fee sofort. */
    private const val MIN_REGION_CELLS = 2

    private const val UNASSIGNED = -1
    private const val MAX_ATTEMPTS = 400

    /** Obergrenze für die Eindeutigkeits-Durchgänge je Versuch. */
    private const val MAX_REFINEMENTS = 200
}
