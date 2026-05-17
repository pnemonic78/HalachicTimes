import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    alias(alibs.plugins.kotlin.jvm)
    alias(alibs.plugins.kotlin.serialization)
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
    implementation(alibs.google.maps)

    testImplementation(alibs.bundles.test)
}
