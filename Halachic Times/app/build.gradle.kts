plugins {
    id("com.android.application")
    id("kotlin-android")
}

fun joinStrings(values: Collection<String>): String {
    return "{\"" + values.joinToString("\", \"") + "\"}"
}

val versionMajor = (project.properties["APP_VERSION_MAJOR"] as String).toInt()
val versionMinor = (project.properties["APP_VERSION_MINOR"] as String).toInt()

android {
    compileSdkVersion(BuildVersions.compileSdkVersion)

    defaultConfig {
        applicationId("net.sf.times")
        minSdkVersion(BuildVersions.minSdkVersion)
        targetSdkVersion(BuildVersions.targetSdkVersion)
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
        resConfigs(locales)
        buildConfigField("String[]", "LOCALES", joinStrings(locales))

        manifestPlaceholders["offline"] = false

        testApplicationId("net.sf.times.test")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = project.properties["STORE_PASSWORD_RELEASE"] as String
            keyAlias = "release"
            keyPassword = project.properties["KEY_PASSWORD_RELEASE"] as String
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            proguardFiles("proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions("internet")
    productFlavors {
        create("normal") {
            dimension = "internet"
        }

        create("offline") {
            dimension = "internet"
            manifestPlaceholders["offline"] = true
        }
    }

    lintOptions {
        disable("GoogleAppIndexingWarning")
        //disable("IconLauncherFormat") // v26 has XML (non-PNG) adaptive icons.
        disable("InconsistentLayout")
        disable("LocaleFolder")
        disable("MergeRootFrame")
        disable("Overdraw")
        disable("PluralsCandidate")
        disable("UnusedAttribute")
    }
}

dependencies {
    implementation(project(":zmanim:lib-android"))
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))
    implementation(project(":locations"))
    implementation(project(":compass-lib"))

    // Background tasks
    implementation("androidx.work:work-runtime:2.5.0")

    // Testing
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test:core:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:runner:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
}
