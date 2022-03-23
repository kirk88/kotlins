import org.gradle.api.JavaVersion

object BuildVersions {
    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 30
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object JavaVersions {
    val sourceCompatibility = JavaVersion.VERSION_1_8
    val targetCompatibility = JavaVersion.VERSION_1_8
}

object KotlinVersions {
    const val jvmTarget = "1.8"
}