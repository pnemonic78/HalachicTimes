import org.gradle.api.JavaVersion

object BuildVersions {
    const val androidGradle = "8.1.4"
    const val kotlinVersion = "1.9.22"
    val jvm = JavaVersion.VERSION_1_8

    const val minSdkVersion = 21
    const val compileSdkVersion = 34
    const val targetSdkVersion = 34

    // App dependencies
    const val androidTestVersion = "1.5.0"
    const val junitExt = "1.1.5"
    const val junitVersion = "4.13.2"
    const val okhttpVersion = "4.8.0"
    const val retrofit2Version = "2.9.0"
    const val roomVersion = "2.3.0"
    const val timberVersion = "5.0.1"
}

object Flavors {
    object Internet {
        const val dimension = "internet"
        const val online = "online"
        const val offline = "offline"
    }
}