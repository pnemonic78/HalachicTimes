buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${BuildVersions.kotlin_version}")

        // Crashlytics
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
