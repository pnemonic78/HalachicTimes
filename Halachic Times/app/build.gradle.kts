import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
}

val versionMajor = project.properties["APP_VERSION_MAJOR"].toString().toInt()
val versionMinor = project.properties["APP_VERSION_MINOR"].toString().toInt()

android {
    compileSdk = libs.versions.compileSdk.toInt()
    namespace = "com.github.times"

    defaultConfig {
        applicationId = "net.sf.times"
        minSdk = libs.versions.minSdk.toInt()
        targetSdk = libs.versions.targetSdk.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = versionMajor * 100 + versionMinor
        versionName = "${versionMajor}.${versionMinor}"

        val locales = listOf(
            "bg",
            "cs",
            "da",
            "de",
            "en",
            "es",
            "es_US",
            "et",
            "fi",
            "fr",
            "hu",
            "it",
            "iw",
            "lt",
            "nb",
            "nl",
            "no",
            "pl",
            "pt",
            "pt_PT",
            "ro",
            "ru",
            "sv",
            "uk"
        )
        resourceConfigurations += locales
        buildConfigField("String[]", "LOCALES", locales.toJavaString())
        buildConfigField("Boolean", "GOOGLE_GCM", "false")
        buildConfigField("Boolean", "INTERNET", "true")
    }

    bundle {
        language {
            // Specifies that the app bundle should not support
            // configuration APKs for language resources. These
            // resources are instead packaged with each base and
            // dynamic feature APK.
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = BuildVersions.jvm
        targetCompatibility = BuildVersions.jvm
    }

    kotlinOptions {
        jvmTarget = BuildVersions.jvm.toString()
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = project.properties["STORE_PASSWORD_RELEASE"] as String
            keyAlias = "release"
            keyPassword = project.properties["KEY_PASSWORD_RELEASE"] as String
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }
    }

    flavorDimensions += Flavors.Internet.dimension
    productFlavors {
        create(Flavors.Internet.development) {
            dimension = Flavors.Internet.dimension
            isDefault = true
            extraProperties["useGoogleGcm"] = false
        }

        create(Flavors.Internet.online) {
            dimension = Flavors.Internet.dimension
            buildConfigField("Boolean", "GOOGLE_GCM", "true")
            extraProperties["useGoogleGcm"] = true
        }

        create(Flavors.Internet.offline) {
            dimension = Flavors.Internet.dimension
            buildConfigField("Boolean", "INTERNET", "false")
            extraProperties["useGoogleGcm"] = false
        }
    }

    lint {
        disable += "GoogleAppIndexingWarning"
        //disable += "IconLauncherFormat" // v26 has XML (non-PNG) adaptive icons.
        disable += "InconsistentLayout"
        disable += "LocaleFolder"
        disable += "MergeRootFrame"
        disable += "Overdraw"
        disable += "PluralsCandidate"
        disable += "UnusedAttribute"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":zmanim"))
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))
    implementation(project(":locations"))
    implementation(project(":compass-lib"))

    // Background tasks
    implementation(libs.work.runtime)

    // Testing
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.test.android)
    implementation(libs.crashlytics)
}

// Disable Google Services plugin for some flavors.
afterEvaluate {
    android.productFlavors.forEach { flavor ->
        val flavorName = flavor.name.capitalize(Locale.ROOT)
        tasks.matching { task ->
            (task.name.endsWith("GoogleServices") || task.name.contains("Crashlytics"))
                    && task.name.contains(flavorName)
        }.forEach { task ->
            task.enabled = flavor.extraProperties["useGoogleGcm"] as Boolean
        }
    }
}
