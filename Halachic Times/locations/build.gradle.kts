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

        buildConfigField(
            "String",
            "BING_API_KEY",
            "\"" + project.properties["BING_API_KEY"] + "\""
        )
        buildConfigField(
            "String",
            "GEONAMES_USERNAME",
            "\"" + project.properties["GEONAMES_USERNAME"] + "\""
        )
        buildConfigField(
            "String",
            "GOOGLE_API_KEY",
            "\"" + project.properties["GOOGLE_API_KEY"] + "\""
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            proguardFiles("proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    lintOptions {
        disable("LocaleFolder")
        disable("RtlHardcoded")
        disable("UnusedAttribute")
        disable("UnusedResources")
    }
}

dependencies {
    implementation(project(":android-lib:lib"))
    implementation(project(":common"))

    // Maps
    implementation("com.google.maps:google-maps-services:0.9.0")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junitVersion}")
    androidTestImplementation("androidx.test:core:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:rules:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test:runner:${BuildVersions.androidTestVersion}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
}
