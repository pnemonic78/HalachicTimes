import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(alibs.plugins.android.library)
    alias(alibs.plugins.kotlin.android)
}

android {
    compileSdk = libs.versions.android.compileSdk.toInt()
    namespace = "com.github.times.compass.lib"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = BuildVersions.jvm
        targetCompatibility = BuildVersions.jvm
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(BuildVersions.jvm.toString())
        }
    }

    lint {
        disable += "LocaleFolder"
        disable += "UnusedResources"
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))
    implementation(project(":locations"))

    // Testing
    testImplementation(alibs.bundles.test)
    androidTestImplementation(alibs.bundles.test.android)
}
