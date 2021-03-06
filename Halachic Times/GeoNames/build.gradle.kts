plugins {
    application
}

application {
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation("javax.json:javax.json-api:1.0")
    implementation("org.glassfish:javax.json:1.0.4")
}