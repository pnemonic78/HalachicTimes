import com.android.build.api.dsl.LibraryDefaultConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64

plugins {
    alias(alibs.plugins.android.library)
    alias(alibs.plugins.kotlin.android)
    alias(alibs.plugins.kotlin.serialization)
}

fun LibraryDefaultConfig.encodeApiKey(name: String) {
    val value = project.properties[name].toString()
    val bytes = value.encodeToByteArray()
    val encoded = Base64.getEncoder().encodeToString(bytes)
    buildConfigField("String", name, "\"" + encoded + "\"")
}

android {
    compileSdk = libs.versions.android.compileSdk.toInt()
    namespace = "com.github.times.location"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.toInt()
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
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }
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
    implementation(alibs.google.maps)
    implementation(alibs.cardview)

    // Background tasks
    implementation(alibs.work.runtime)

    testImplementation(alibs.bundles.test)
    testImplementation(kotlin("reflect"))
    androidTestImplementation(alibs.bundles.test.android)
}
