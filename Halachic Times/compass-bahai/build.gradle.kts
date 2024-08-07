plugins {
    id("com.android.application")
    kotlin("android")
}

val versionMajor = project.properties["APP_VERSION_MAJOR"].toString().toInt()
val versionMinor = project.properties["APP_VERSION_MINOR"].toString().toInt()

android {
    compileSdk = BuildVersions.compileSdk
    namespace = "com.github.compass.bahai"

    defaultConfig {
        applicationId = "net.sf.times.compass.bahai"
        minSdk = BuildVersions.minSdk
        targetSdk = BuildVersions.targetSdk
        versionCode = versionMajor * 100 + versionMinor
        versionName = "${versionMajor}." + versionMinor.toString().padStart(2, '0')

        val locales = listOf(
            "ar",
            "bg",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fa",
            "fi",
            "fr",
            "hi",
            "hu",
            "it",
            "iw",
            "lt",
            "nb",
            "nl",
            "no",
            "pl",
            "pt",
            "ro",
            "ru",
            "sv",
            "tr",
            "uk"
        )
        resourceConfigurations += locales
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
        disable += "InconsistentLayout"
        disable += "LocaleFolder"
        disable += "Overdraw"
        disable += "PluralsCandidate"
        disable += "UnusedAttribute"
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))
    implementation(project(":locations"))
    implementation(project(":compass-lib"))
}
