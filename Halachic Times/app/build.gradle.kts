plugins {
    id("com.android.application")
    kotlin("android")

    // Add the Firebase Crashlytics plugin.
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

fun joinStrings(values: Collection<String>): String {
    return "{\"" + values.joinToString("\", \"") + "\"}"
}

val versionMajor = project.properties["APP_VERSION_MAJOR"].toString().toInt()
val versionMinor = project.properties["APP_VERSION_MINOR"].toString().toInt()

android {
    compileSdk = BuildVersions.compileSdk
    namespace = "com.github.times"

    defaultConfig {
        applicationId = "net.sf.times"
        minSdk = BuildVersions.minSdk
        targetSdk = BuildVersions.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = versionMajor * 100 + versionMinor
        versionName = "${versionMajor}." + versionMinor.toString().padStart(2, '0')

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

        buildConfigField("String[]", "LOCALES", joinStrings(locales))
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
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }
    }

    flavorDimensions += Flavors.Internet.dimension
    productFlavors {
        create(Flavors.Internet.online) {
            dimension = Flavors.Internet.dimension
            isDefault = true
        }

        create(Flavors.Internet.offline) {
            dimension = Flavors.Internet.dimension
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
}

dependencies {
    implementation(project(":zmanim"))
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))
    implementation(project(":locations"))
    implementation(project(":compass-lib"))

    // Background tasks
    implementation("androidx.work:work-runtime:${BuildVersions.work}")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junit}")
    androidTestImplementation("androidx.test:core:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test:runner:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test.ext:junit:${BuildVersions.junitExt}")
    androidTestImplementation(project(":android-lib:lib"))
    /// Declare the dependencies for the Crashlytics and Analytics libraries
    implementation("com.google.firebase:firebase-crashlytics:19.0.3")
}
