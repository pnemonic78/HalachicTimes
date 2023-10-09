plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = BuildVersions.compileSdkVersion
    namespace = "com.github.times.common"

    defaultConfig {
        minSdk = BuildVersions.minSdkVersion
        targetSdk = BuildVersions.targetSdkVersion
    }

    compileOptions {
        sourceCompatibility = BuildVersions.jvm
        targetCompatibility = BuildVersions.jvm
    }

    kotlinOptions {
        jvmTarget = BuildVersions.jvm.toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFile("proguard-rules.pro")
        }
    }

    lint {
        disable += "LocaleFolder"
        disable += "UnusedResources"
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
}
