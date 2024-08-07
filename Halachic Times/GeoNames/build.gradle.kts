import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version BuildVersions.kotlin
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
    implementation(project(":android-lib:kvm"))

    // Maps
    implementation("com.google.maps:google-maps-services:2.1.0")

    // Testing
    testImplementation("junit:junit:${BuildVersions.junit}")
}
