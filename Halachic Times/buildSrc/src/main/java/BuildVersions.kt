object BuildVersions {
    const val kotlin_version = "1.7.20"

    const val minSdkVersion = 21
    const val compileSdkVersion = 33
    const val targetSdkVersion = 33

    // App dependencies
    const val androidTestVersion = "1.5.0"
    const val junitVersion = "4.13.2"
    const val junitExt = "1.1.5"
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