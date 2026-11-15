import org.gradle.api.JavaVersion

object BuildVersions {
    val jvm = JavaVersion.VERSION_11
}

object Flavors {
    object Internet {
        const val dimension = "internet"
        const val development = "development"
        const val online = "online"
        const val offline = "offline"
    }
}