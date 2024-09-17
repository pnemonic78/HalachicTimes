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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    testImplementation("org.robolectric:robolectric:${BuildVersions.robolectric}")
    testImplementation(kotlin("reflect"))
    androidTestImplementation("androidx.test:core-ktx:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTest}")
    androidTestImplementation("androidx.test.ext:junit:${BuildVersions.junitExt}")
}
