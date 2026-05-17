import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(alibs.plugins.android.library)
    alias(alibs.plugins.kotlin.android)
}

android {
    compileSdk = libs.versions.android.compileSdk.toInt()
    namespace = "com.github.times.common"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.toInt()

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
            "pt_PT",
            "ro",
            "ru",
            "sv",
            "tr",
            "uk"
        )
        resourceConfigurations += locales
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
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

    testImplementation(alibs.bundles.test)
}
