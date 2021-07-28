plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = BuildVersions.compileSdkVersion

    defaultConfig {
        minSdk = BuildVersions.minSdkVersion
        targetSdk = BuildVersions.targetSdkVersion
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFile("proguard-rules.pro")
        }
    }

    lint {
        disable("LocaleFolder")
        disable("UnusedResources")
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
}
