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

val versionMajor = (project.properties["APP_VERSION_MAJOR"] as String).toInt()
val versionMinor = (project.properties["APP_VERSION_MINOR"] as String).toInt()

android {
    compileSdk = BuildVersions.compileSdkVersion

    defaultConfig {
        applicationId = "net.sf.times"
        minSdk = BuildVersions.minSdkVersion
        targetSdk = BuildVersions.targetSdkVersion
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

        testApplicationId = "net.sf.times.test"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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
    implementation("androidx.work:work-runtime:2.7.1")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junitVersion}")
    androidTestImplementation("androidx.test:core:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:runner:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    /// Declare the dependencies for the Crashlytics and Analytics libraries
    implementation("com.google.firebase:firebase-crashlytics:18.2.9")
}
