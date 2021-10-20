enableFeaturePreview("VERSION_CATALOGS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.1.0-beta01"
        id("com.android.library") version "7.1.0-beta01"
        id("org.jetbrains.kotlin.android") version "1.5.31"
        id("com.github.ben-manes.versions") version "0.39.0"
        id("org.jetbrains.kotlin.jvm") version "1.5.31"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://dl.google.com/dl/android/maven2")
        maven("https://jitpack.io")
    }

    versionCatalogs {
        create("androids") {
            version("compileSdk", "31")
            version("minSdk", "21")
            version("targetSdk", "30")
        }

        create("libs") {
            version("coroutines", "1.5.2")
            version("lifecycle", "2.4.0-rc01")
            version("datastore", "1.0.0")
            version("sqlite", "2.2.0-beta01")
            version("okhttp", "5.0.0-alpha.2")

            alias("kotlin-stdlib").to(
                "org.jetbrains.kotlin",
                "kotlin-stdlib"
            ).version("1.5.31")

            alias("kotlinx-coroutines-core").to(
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).versionRef("coroutines")
            alias("kotlinx-coroutines-android").to(
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-android"
            ).versionRef("coroutines")
            bundle(
                "kotlinx-coroutines",
                listOf(
                    "kotlinx-coroutines-core",
                    "kotlinx-coroutines-android"
                )
            )

            alias("androidx-lifecycle-viewmodel-ktx").to(
                "androidx.lifecycle",
                "lifecycle-viewmodel-ktx"
            ).versionRef("lifecycle")
            alias("androidx-lifecycle-viewmodel-savedstate").to(
                "androidx.lifecycle",
                "lifecycle-viewmodel-savedstate"
            ).versionRef("lifecycle")
            alias("androidx-lifecycle-runtime-ktx").to(
                "androidx.lifecycle",
                "lifecycle-runtime-ktx"
            ).versionRef("lifecycle")
            alias("androidx-lifecycle-livedata-ktx").to(
                "androidx.lifecycle",
                "lifecycle-livedata-ktx"
            ).versionRef("lifecycle")
            bundle(
                "androidx-lifecycle",
                listOf(
                    "androidx-lifecycle-viewmodel-ktx",
                    "androidx-lifecycle-viewmodel-savedstate",
                    "androidx-lifecycle-runtime-ktx",
                    "androidx-lifecycle-livedata-ktx"
                )
            )

            alias("androidx-core-ktx").to(
                "androidx.core",
                "core-ktx"
            ).version("1.7.0-rc01")
            alias("androidx-activity-ktx").to(
                "androidx.activity",
                "activity-ktx"
            ).version("1.4.0-rc01")
            alias("androidx-fragment-ktx").to(
                "androidx.fragment",
                "fragment-ktx"
            ).version("1.4.0-alpha10")
            alias("androidx-appcompat").to(
                "androidx.appcompat",
                "appcompat"
            ).version("1.4.0-beta01")
            bundle(
                "androidx-common-app",
                listOf(
                    "androidx-core-ktx",
                    "androidx-activity-ktx",
                    "androidx-fragment-ktx",
                    "androidx-appcompat"
                )
            )

            alias("androidx-recyclerview").to(
                "androidx.recyclerview",
                "recyclerview"
            ).version("1.3.0-alpha01")
            alias("androidx-constraintlayout").to(
                "androidx.constraintlayout",
                "constraintlayout"
            ).version("2.1.1")
            alias("androidx-swiperefreshlayout").to(
                "androidx.swiperefreshlayout",
                "swiperefreshlayout"
            ).version("1.2.0-alpha01")
            bundle(
                "androidx-common-view",
                listOf(
                    "androidx-recyclerview",
                    "androidx-constraintlayout",
                    "androidx-swiperefreshlayout"
                )
            )

            alias("androidx-annotation").to(
                "androidx.annotation",
                "annotation"
            ).version("1.3.0-beta01")
            alias("androidx-setup").to(
                "androidx.startup",
                "startup-runtime"
            ).version("1.1.0")

            alias("androidx-datastore-core").to(
                "androidx.datastore",
                "datastore-core"
            ).versionRef("datastore")
            alias("androidx-datastore-preferences").to(
                "androidx.datastore",
                "datastore-preferences"
            ).versionRef("datastore")
            bundle(
                "androidx-datastore",
                listOf(
                    "androidx-datastore-core",
                    "androidx-datastore-preferences"
                )
            )

            alias("androidx-sqlite-ktx").to(
                "androidx.sqlite",
                "sqlite-ktx"
            ).versionRef("sqlite")
            alias("androidx-sqlite-framework").to(
                "androidx.sqlite",
                "sqlite-framework"
            ).versionRef("sqlite")
            bundle(
                "androidx-sqlite",
                listOf(
                    "androidx-sqlite-ktx",
                    "androidx-sqlite-framework"
                )
            )

            alias("androidx-multidex").to(
                "androidx.multidex",
                "multidex"
            ).version("2.0.1")

            alias("google-material").to(
                "com.google.android.material",
                "material"
            ).version("1.5.0-alpha04")
            alias("google-gson").to(
                "com.google.code.gson",
                "gson"
            ).version("2.8.8")

            alias("okhttp3-okhttp").to(
                "com.squareup.okhttp3",
                "okhttp"
            ).versionRef("okhttp")
            alias("okhttp3-logging").to(
                "com.squareup.okhttp3",
                "logging-interceptor"
            ).versionRef("okhttp")
            bundle(
                "okhttp3",
                listOf(
                    "okhttp3-okhttp",
                    "okhttp3-logging"
                )
            )

            alias("coil").to(
                "io.coil-kt",
                "coil"
            ).version("2.0.0-alpha01")
        }
    }
}
rootProject.name = "kotlins"
rootProject.buildFileName = "build.gradle.kts"
include(
    ":kot-atomic",
    ":kot-bluetooth",
    ":kot-common",
    ":kot-sqlite",
    ":kot-okhttp",
    "kot-gson",
    ":sample"
)