import org.gradle.api.JavaVersion

object BuildVersions {
    const val agp = "8.7.3"
    const val kotlin = "2.1.0"
    val jvm = JavaVersion.VERSION_1_8

    const val minSdk = 21
    const val compileSdk = 35
    const val targetSdk = 35

    // App dependencies
    const val androidTest = "1.6.1"
    const val junit = "4.13.2"
    const val junitExt = "1.2.1"
    const val okhttp = "4.12.0"
    const val retrofit = "2.9.0"
    const val room = "2.6.1"
    const val robolectric = "4.14.1"
    const val timber = "5.0.1"
    const val work = "2.10.0"
}

object Flavors {
    object Internet {
        const val dimension = "internet"
        const val development = "development"
        const val online = "online"
        const val offline = "offline"
    }
}