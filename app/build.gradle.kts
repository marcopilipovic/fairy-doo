import java.util.Properties

// Liegt bewusst außerhalb von Git (siehe .gitignore) — enthält den Signier-
// Schlüssel-Zugang. Ohne diese Datei (z. B. bei jemand anderem, der das
// Repo frisch auscheckt) bleibt der Release-Build unsigniert buildbar,
// nur eben nicht installierbar, bis eine eigene keystore.properties angelegt wird.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.fairydoo.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fairydoo.game"
        minSdk = 26
        targetSdk = 36
        // Die Nummer darf nie kleiner werden — Android lehnt jede Installation
        // ab, deren versionCode unter dem liegt, was auf dem Gerät steht. Die
        // beiden Linien hatten bis zur Zusammenführung eigene Zählungen: main
        // stand bei 7, der Veröffentlichungszweig bei 1. Übernommen wurde
        // versehentlich die 1, und damit ließ sich die App auf keinem Telefon
        // mehr aktualisieren, auf dem eine Testfassung von main lag.
        //
        // Seither zählt sie über beiden bisherigen Ständen weiter und wird bei
        // jeder Fassung erhöht, die auf ein Telefon geht. Für den Store ist die
        // Zahl der ersten Einreichung beliebig; nur steigen muss sie danach.
        versionCode = 53
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    // Echte Werbekennungen nur in der Release-Fassung.
    //
    // Der Grund ist kein technischer, sondern ein wirtschaftlicher: Wer auf
    // seine eigenen echten Anzeigen tippt, erzeugt für Google „ungültigen
    // Traffic". Das ist der häufigste Weg, ein AdMob-Konto zu verlieren, und es
    // trifft ausgerechnet die, die ihre App gewissenhaft durchtesten.
    //
    // Deshalb behält die Debug-Fassung Googles Testkennungen. Auf ihnen darf
    // man tippen, so oft man will. Wer die Release-Fassung testet, sollte sein
    // Gerät zusätzlich im AdMob-Konto als Testgerät eintragen.
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-5051364478140655~5511669323"
            buildConfigField("String", "AD_UNIT_ID", "\"ca-app-pub-5051364478140655/4643626005\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystoreProperties.containsKey("storeFile")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
