import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Die Feature-Grafik für den Play Store: 1024 x 500.
 *
 * Farben und Schriften stammen aus dem Spiel selbst — Color.kt und
 * app/src/main/res/font/. Der Banner soll aussehen wie ein Ausschnitt des
 * Nachtwalds, nicht wie ein Werbebild daneben.
 *
 * Der Schriftzug steht bewusst mittig und ohne Zusatz: Google beschneidet die
 * Grafik je nach Ansicht an den Rändern, und der App-Name steht ohnehin
 * daneben. Was hier zählt, ist die Stimmung.
 */
public class FeatureGrafik {

    static final int W = 1024, H = 500;

    // Grundflächen — der gemalte blaugrün-violette Nachtwald
    static final Color NIGHT_TOP    = new Color(0x0A2032);
    static final Color NIGHT_MIDDLE = new Color(0x12283E);
    static final Color NIGHT_DEEP   = new Color(0x1C1F4A);
    static final Color NIGHT_BOTTOM = new Color(0x241A4A);
    static final Color NIGHT_HALO   = new Color(0x0E2E42);

    // Die farbigen Schimmer von den Rändern her
    static final Color GLOW_PINK   = new Color(0xFF, 0x78, 0xBE, 0x2A);
    static final Color GLOW_VIOLET = new Color(0xA0, 0x6E, 0xFF, 0x28);
    static final Color GLOW_TEAL   = new Color(0x50, 0xC8, 0xBE, 0x14);

    // Der Verlauf des Titels — Creme oben, warmes Gold unten
    static final Color TITLE_TOP    = new Color(0xFFFBE8);
    static final Color TITLE_MIDDLE = new Color(0xFFEDB0);
    static final Color TITLE_BOTTOM = new Color(0xF2C060);

    static final Color STATUS_PURPLE = new Color(0xC9C2FF);
    static final Color FIREFLY       = new Color(0xFFE9A8);
    static final Color TREE_DARK     = new Color(0x14, 0x2B, 0x22);

    public static void main(String[] args) throws Exception {
        String fontDir = args[0];
        File out = new File(args[1]);

        Font cinzel = Font.createFont(Font.TRUETYPE_FONT,
                new File(fontDir, "cinzel_decorative_bold.ttf"));
        Font quicksand = Font.createFont(Font.TRUETYPE_FONT,
                new File(fontDir, "quicksand_variable.ttf"));

        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackdrop(g);
        drawForest(g);
        drawGround(g);
        drawMushrooms(g);
        drawFireflies(g);
        drawWordmark(g, cinzel);
        drawSubtitle(g, quicksand);
        drawVignette(g);

        g.dispose();
        ImageIO.write(img, "png", out);
        System.out.println(out.getAbsolutePath() + "  " + W + "x" + H);
    }

    /** Vier Verlaufsschichten wie NightBackdrop im Spiel. */
    static void drawBackdrop(Graphics2D g) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(0, 0), new Point2D.Float(0, H),
                new float[]{0f, 0.40f, 0.75f, 1f},
                new Color[]{NIGHT_TOP, NIGHT_MIDDLE, NIGHT_DEEP, NIGHT_BOTTOM}));
        g.fillRect(0, 0, W, H);

        // Der dunkle Bogen am oberen Rand
        radial(g, W * 0.5f, -H * 0.25f, H * 1.05f, NIGHT_HALO);
        // Rosa und Violett von unten, Türkis von den Seiten
        radial(g, W * 0.06f, H * 1.25f, W * 0.48f, GLOW_PINK);
        radial(g, W * 0.94f, H * 1.25f, W * 0.48f, GLOW_VIOLET);
        radial(g, W * 0.5f, H * 0.20f, W * 0.34f, GLOW_TEAL);
    }

    static void radial(Graphics2D g, float cx, float cy, float r, Color c) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(cx, cy), r,
                new float[]{0f, 1f},
                new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}));
        g.fillRect(0, 0, W, H);
    }

    /**
     * Tannen an beiden Rändern, gestaffelt nach Tiefe.
     *
     * Nur an den Rändern: Die Mitte bleibt frei für den Schriftzug, und dort
     * beschneidet Google die Grafik am wenigsten.
     */
    static void drawForest(Graphics2D g) {
        Random rnd = new Random(7);
        // Hintere Reihe, blasser und kleiner
        for (int i = 0; i < 40; i++) {
            boolean left = i % 2 == 0;
            float x = left ? rnd.nextFloat() * 300 : W - rnd.nextFloat() * 300;
            float h = 110 + rnd.nextFloat() * 70;
            tree(g, x, H * 0.90f, h, new Color(TREE_DARK.getRed(), TREE_DARK.getGreen(),
                    TREE_DARK.getBlue(), 120));
        }
        // Vordere Reihe, dunkler und größer
        for (int i = 0; i < 22; i++) {
            boolean left = i % 2 == 0;
            float x = left ? rnd.nextFloat() * 240 : W - rnd.nextFloat() * 240;
            float h = 170 + rnd.nextFloat() * 120;
            tree(g, x, H * 0.95f, h, new Color(TREE_DARK.getRed(), TREE_DARK.getGreen(),
                    TREE_DARK.getBlue(), 210));
        }
    }

    /** Eine Tanne als drei gestapelte Dreiecke — dieselbe Form wie im Spiel. */
    static void tree(Graphics2D g, float x, float baseY, float h, Color c) {
        g.setColor(c);
        float w = h * 0.52f;
        for (int tier = 0; tier < 3; tier++) {
            float ty = baseY - h * (0.30f + tier * 0.26f);
            float tw = w * (1f - tier * 0.22f);
            float th = h * 0.42f;
            Path2D p = new Path2D.Float();
            p.moveTo(x, ty - th);
            p.lineTo(x - tw / 2, ty);
            p.lineTo(x + tw / 2, ty);
            p.closePath();
            g.fill(p);
        }
        // Kein Stamm: Bei dieser Größe ragte er als dunkler Klotz über die
        // Waldkante hinaus. Die Kronen allein lesen sich als Wald.
    }

    /**
     * Die Waldkante am unteren Rand.
     *
     * Ohne sie stehen die Tannen auf nichts — ihre Stämme hingen sichtbar in
     * der Luft, und der Banner franste nach unten aus. Zwei flache Hügel
     * genügen; sie verdecken die Stämme und geben dem Bild einen Abschluss.
     */
    static void drawGround(Graphics2D g) {
        Path2D hill = new Path2D.Float();
        hill.moveTo(-20, H + 10);
        hill.lineTo(-20, H * 0.86f);
        hill.curveTo(W * 0.18f, H * 0.78f, W * 0.34f, H * 0.90f, W * 0.52f, H * 0.86f);
        hill.curveTo(W * 0.70f, H * 0.82f, W * 0.86f, H * 0.92f, W + 20, H * 0.84f);
        hill.lineTo(W + 20, H + 10);
        hill.closePath();

        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(0, H * 0.78f), new Point2D.Float(0, H),
                new float[]{0f, 1f},
                new Color[]{new Color(0x16, 0x2C, 0x24), new Color(0x0C, 0x16, 0x1E)}));
        g.fill(hill);

        // Ein schmaler heller Saum auf der Kuppe — dort fängt sich das Mondlicht.
        g.setColor(new Color(0x8C, 0xBE, 0x5A, 46));
        g.setStroke(new BasicStroke(2.4f));
        g.draw(hill);
    }

    /**
     * Leuchtpilze auf der Waldkante.
     *
     * Im Spiel stehen sie am Feenpfad und geben ihm seinen Charakter — ohne sie
     * wäre der Banner ein beliebiger Nachthimmel mit Tannen. Nur wenige und nur
     * am Rand, damit die Mitte frei bleibt.
     */
    static void drawMushrooms(Graphics2D g) {
        mushroom(g, W * 0.13f, H * 0.895f, 17f, new Color(0xFF, 0x9E, 0xCF));
        mushroom(g, W * 0.21f, H * 0.925f, 11f, new Color(0xFF, 0xD7, 0x6B));
        mushroom(g, W * 0.84f, H * 0.905f, 15f, new Color(0xC5, 0x8B, 0xFF));
        mushroom(g, W * 0.92f, H * 0.935f, 10f, new Color(0x6B, 0xFF, 0xF2));
    }

    /** Stiel, Hut, Tupfen — und der Lichthof, der ihn leuchten lässt. */
    static void mushroom(Graphics2D g, float x, float y, float s, Color cap) {
        float glow = s * 3.4f;
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(x, y - s * 0.25f), glow,
                new float[]{0f, 1f},
                new Color[]{new Color(cap.getRed(), cap.getGreen(), cap.getBlue(), 52),
                        new Color(cap.getRed(), cap.getGreen(), cap.getBlue(), 0)}));
        g.fill(new Ellipse2D.Float(x - glow, y - s * 0.25f - glow, glow * 2, glow * 2));

        g.setColor(new Color(0xF6, 0xEA, 0xD2, 210));
        g.fill(new RoundRectangle2D.Float(x - s * 0.16f, y - s * 0.55f,
                s * 0.32f, s * 0.62f, s * 0.14f, s * 0.14f));

        g.setColor(cap);
        g.fill(new Arc2D.Float(x - s * 0.62f, y - s * 1.00f, s * 1.24f, s * 0.94f,
                0, 180, Arc2D.CHORD));

        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Float(x - s * 0.30f, y - s * 0.80f, s * 0.20f, s * 0.16f));
        g.fill(new Ellipse2D.Float(x + s * 0.10f, y - s * 0.68f, s * 0.15f, s * 0.12f));
    }

    /** Glühwürmchen: heller Kern, weicher Hof. */
    static void drawFireflies(Graphics2D g) {
        Random rnd = new Random(21);
        for (int i = 0; i < 46; i++) {
            float x = rnd.nextFloat() * W;
            float y = rnd.nextFloat() * H;
            // In der Mitte weniger, damit die Schrift ruhig bleibt
            if (Math.abs(x - W / 2f) < 210 && Math.abs(y - H * 0.46f) < 90 && rnd.nextFloat() < 0.8f) {
                continue;
            }
            float r = 1.6f + rnd.nextFloat() * 2.6f;
            float glow = r * 6f;
            int alpha = 70 + rnd.nextInt(120);

            g.setPaint(new RadialGradientPaint(
                    new Point2D.Float(x, y), glow,
                    new float[]{0f, 1f},
                    new Color[]{new Color(FIREFLY.getRed(), FIREFLY.getGreen(),
                            FIREFLY.getBlue(), alpha / 3),
                            new Color(FIREFLY.getRed(), FIREFLY.getGreen(),
                                    FIREFLY.getBlue(), 0)}));
            g.fill(new Ellipse2D.Float(x - glow, y - glow, glow * 2, glow * 2));

            g.setColor(new Color(FIREFLY.getRed(), FIREFLY.getGreen(), FIREFLY.getBlue(),
                    Math.min(255, alpha + 90)));
            g.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
        }
    }

    /**
     * „FAIRYDOKU" im Goldverlauf, flankiert von zwei Funken.
     *
     * Als Umriss gezeichnet statt als Text, damit der Verlauf exakt über die
     * Buchstaben läuft und nicht über den Zeilenkasten.
     */
    static void drawWordmark(Graphics2D g, Font cinzel) {
        Font font = cinzel.deriveFont(Font.BOLD, 96f);
        g.setFont(font);
        String text = "FAIRYDOKU";
        GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), text);
        Shape glyphs = gv.getOutline();
        Rectangle2D b = glyphs.getBounds2D();

        double cx = W / 2.0 - b.getCenterX();
        double cy = H * 0.46 - b.getCenterY();
        Shape placed = AffineTransform.getTranslateInstance(cx, cy).createTransformedShape(glyphs);
        Rectangle2D pb = placed.getBounds2D();

        // Weicher Schein darunter, damit der Schriftzug vom Wald abhebt
        for (int i = 5; i >= 1; i--) {
            g.setColor(new Color(0xFF, 0xD7, 0x6B, 8));
            g.setStroke(new BasicStroke(i * 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(placed);
        }
        // Dunkler Absatz nach unten für Tiefe
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(AffineTransform.getTranslateInstance(0, 4).createTransformedShape(placed));

        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, pb.getMinY()), new Point2D.Double(0, pb.getMaxY()),
                new float[]{0f, 0.45f, 1f},
                new Color[]{TITLE_TOP, TITLE_MIDDLE, TITLE_BOTTOM}));
        g.fill(placed);

        float starY = (float) pb.getCenterY();
        star(g, (float) pb.getMinX() - 52, starY, 17);
        star(g, (float) pb.getMaxX() + 52, starY, 17);
    }

    /** Ein vierzackiger Funke — dasselbe ✦ wie im Spiel, nur gezeichnet. */
    static void star(Graphics2D g, float cx, float cy, float r) {
        Path2D p = new Path2D.Float();
        float w = r * 0.30f;
        p.moveTo(cx, cy - r);
        p.quadTo(cx + w * 0.4f, cy - w * 0.4f, cx + r, cy);
        p.quadTo(cx + w * 0.4f, cy + w * 0.4f, cx, cy + r);
        p.quadTo(cx - w * 0.4f, cy + w * 0.4f, cx - r, cy);
        p.quadTo(cx - w * 0.4f, cy - w * 0.4f, cx, cy - r);
        p.closePath();

        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(cx, cy), r * 2.6f,
                new float[]{0f, 1f},
                new Color[]{new Color(0xFF, 0xE9, 0xA8, 60), new Color(0xFF, 0xE9, 0xA8, 0)}));
        g.fill(new Ellipse2D.Float(cx - r * 2.6f, cy - r * 2.6f, r * 5.2f, r * 5.2f));

        g.setColor(TITLE_TOP);
        g.fill(p);
    }

    /** Die Zeile darunter: kurz genug, um auch klein noch lesbar zu sein. */
    static void drawSubtitle(Graphics2D g, Font quicksand) {
        Font font = quicksand.deriveFont(Font.PLAIN, 27f);
        g.setFont(font);
        String text = "Ein Logikrätsel im Nachtwald";

        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int x = (W - tw) / 2;
        int y = (int) (H * 0.46) + 92;

        g.setColor(new Color(0, 0, 0, 110));
        g.drawString(text, x, y + 2);
        g.setColor(STATUS_PURPLE);
        g.drawString(text, x, y);
    }

    /** Ganz leichte Abdunklung an den Rändern — hält den Blick in der Mitte. */
    static void drawVignette(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(W / 2f, H / 2f), W * 0.62f,
                new float[]{0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 90)}));
        g.fillRect(0, 0, W, H);
    }
}
