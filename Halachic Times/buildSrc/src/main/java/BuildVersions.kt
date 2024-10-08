import org.gradle.api.JavaVersion

object BuildVersions {
    const val agp = "8.4.2"
    const val kotlin = "2.0.0"
    val jvm = JavaVersion.VERSION_1_8

    const val minSdk = 21
    const val compileSdk = 34
    const val targetSdk = 34

    // App dependencies
    const val androidTest = "1.6.1"
    const val junit = "4.13.2"
    const val junitExt = "1.2.1"
    const val okhttp = "4.12.0"
    const val retrofit = "2.9.0"
    const val room = "2.6.1"
    const val robolectric = "4.13"
    const val timber = "5.0.1"
    const val work = "2.9.1"
}

object Flavors {
    object Internet {
        const val dimension = "internet"
        const val online = "online"
        const val offline = "offline"
    }
}