import com.android.build.api.dsl.LibraryDefaultConfig
import java.util.Base64

plugins {
    id("com.android.library")
    kotlin("android")
}

fun LibraryDefaultConfig.encodeApiKey(name: String) {
    val value = project.properties[name].toString()
    val bytes = value.encodeToByteArray()
    val encoded = Base64.getEncoder().encodeToString(bytes)
    buildConfigField("String", name, "\"" + encoded + "\"")
}

android {
    compileSdk = BuildVersions.compileSdkVersion

    defaultConfig {
        minSdk = BuildVersions.minSdkVersion
        targetSdk = BuildVersions.targetSdkVersion

        encodeApiKey("BING_API_KEY",)
        encodeApiKey("GEONAMES_USERNAME",)
        encodeApiKey("GOOGLE_API_KEY",)
    }

    buildTypes {
        getByName("release") {
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
            buildConfigField(
                "Boolean",
                "INTERNET",
                "true"
            )
            isDefault = true
        }

        create(Flavors.Internet.offline) {
            dimension = Flavors.Internet.dimension
            buildConfigField(
                "Boolean",
                "INTERNET",
                "false"
            )
        }
    }

    lint {
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
