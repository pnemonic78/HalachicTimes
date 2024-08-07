import com.android.build.api.dsl.LibraryDefaultConfig
import java.util.Base64

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization") version BuildVersions.kotlin
}

fun LibraryDefaultConfig.encodeApiKey(name: String) {
    val value = project.properties[name].toString()
    val bytes = value.encodeToByteArray()
    val encoded = Base64.getEncoder().encodeToString(bytes)
    buildConfigField("String", name, "\"" + encoded + "\"")
}

android {
    compileSdk = BuildVersions.compileSdk
    namespace = "com.github.times.location"

    defaultConfig {
        minSdk = BuildVersions.minSdk
        targetSdk = BuildVersions.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        encodeApiKey("BING_API_KEY")
        encodeApiKey("GEONAMES_USERNAME")
        encodeApiKey("GOOGLE_API_KEY")

        val locales = listOf(
            "ar",
            "bg",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "es_US",
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    flavorDimensions += Flavors.Internet.dimension
    productFlavors {
        create(Flavors.Internet.online) {
            dimension = Flavors.Internet.dimension
            buildConfigField("Boolean", "INTERNET", "true")
            isDefault = true
        }

        create(Flavors.Internet.offline) {
            dimension = Flavors.Internet.dimension
            buildConfigField("Boolean", "INTERNET", "false")
        }
    }

    compileOptions {
        sourceCompatibility = BuildVersions.jvm
        targetCompatibility = BuildVersions.jvm
    }

    kotlinOptions {
        jvmTarget = BuildVersions.jvm.toString()
    }

    lint {
        disable += "LocaleFolder"
        disable += "RtlHardcoded"
        disable += "UnusedAttribute"
        disable += "UnusedResources"
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))

    // Maps
    implementation("com.google.maps:google-maps-services:2.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Background tasks
    implementation("androidx.work:work-runtime:${BuildVersions.work}")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junit}")
    androidTestImplementation("androidx.test:core:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test:runner:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test.ext:junit:${BuildVersions.junitExt}")
    androidTestImplementation(kotlin("reflect"))
}
