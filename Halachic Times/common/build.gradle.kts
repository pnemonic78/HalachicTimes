plugins {
    id("com.android.library")
    id("kotlin-android")
}

val versionMajor = (project.properties["LIB_VERSION_MAJOR"] as String).toInt()
val versionMinor = (project.properties["LIB_VERSION_MINOR"] as String).toInt()

android {
    compileSdkVersion(BuildVersions.compileSdkVersion)

    defaultConfig {
        minSdkVersion(BuildVersions.minSdkVersion)
        targetSdkVersion(BuildVersions.targetSdkVersion)
        versionCode = versionMajor * 100 + versionMinor
        versionName = "${versionMajor}." + versionMinor.toString().padStart(2, '0')
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFile("proguard-rules.pro")
        }
    }

    lintOptions {
        disable("LocaleFolder")
        disable("UnusedResources")
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
}
