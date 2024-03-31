import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version BuildVersions.kotlinVersion
}

repositories {
    mavenCentral()
}

application {
    java {
        sourceCompatibility = BuildVersions.jvm
        targetCompatibility = BuildVersions.jvm
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(BuildVersions.jvm.toString()))
        }
    }

    sourceSets {
        main {
            resources.srcDir("res")
            kotlin {
                srcDir("../android-lib/kotlin/src/main/kotlin")
            }
        }
    }

    mainClass.set("com.github.geonames.JewishCities")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.github.geonames.JewishCities")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Maps
    implementation("com.google.maps:google-maps-services:2.1.0")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junitVersion}")
}
