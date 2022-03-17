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

object KotlinOptions {
    const val jvmTarget = "1.8"
    val compilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}

internal object Versions {
    const val kotlin = "1.6.10"
    const val coroutines = "1.6.0"
    const val coreKtx = "1.7.0"
    const val activityKtx = "1.4.0"
    const val fragmentKtx = "1.4.1"
    const val appcompat = "1.4.1"
    const val material = "1.5.0"
    const val lifecycle = "2.4.1"
    const val annotation = "1.3.0"
    const val startup = "1.1.1"
    const val multidex = "2.0.1"
    const val datastore = "1.0.0"
    const val sqlite = "2.2.0"
    const val okhttp = "4.9.2"
    const val gson = "2.8.9"
    const val coil = "2.0.0"
}

object Libs {
    const val KotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val KotlinStdlibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"

    const val CoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val CoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val ActivityKtx = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    const val FragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"

    const val CoreKtx = "androidx.core:core-ktx:1.3.2:${Versions.coreKtx}"
    const val Appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val Material = "com.google.android.material:material:${Versions.material}"

    const val LifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val LifecycleViewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle}"
    const val LifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"

    const val Annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val Startup = "androidx.startup:startup-runtime:${Versions.startup}"
    const val Multidex = "androidx.multidex:multidex:${Versions.multidex}"

    const val DataStoreCore = "androidx.datastore:datastore-core:${Versions.datastore}"
    const val DataStorePreferences = "androidx.datastore:datastore-preferences:${Versions.datastore}"

    const val SqliteKtx = "androidx.sqlite:sqlite-ktx:${Versions.sqlite}"
    const val SqliteFramework = "androidx.sqlite:sqlite-framework:${Versions.sqlite}"

    const val OkHttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val OkHttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"

    const val Gson = "com.google.code.gson:gson:${Versions.gson}"

    const val Coil = "io.coil-kt:coil:${Versions.coil}"
}