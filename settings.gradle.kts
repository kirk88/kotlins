enableFeaturePreview("VERSION_CATALOGS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.0.3"
        id("com.android.library") version "7.0.3"
        id("org.jetbrains.kotlin.android") version "1.5.31"
        id("org.jetbrains.kotlin.jvm") version "1.5.31"
        id("com.github.ben-manes.versions") version "0.39.0"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://dl.google.com/dl/android/maven2")
    }

    versionCatalogs {
        create("app") {
            version("compileSdk", "31")
            version("minSdk", "21")
            version("targetSdk", "30")
        }

        create("kotlinLibs") {
            version("jvmTarget", "11")

            val versionKotlinStdlib = "1.5.31"
            alias("stdlib").to("org.jetbrains.kotlin", "kotlin-stdlib")
                .version(versionKotlinStdlib)
            alias("stdlib-common").to("org.jetbrains.kotlin", "kotlin-stdlib-common")
                .version(versionKotlinStdlib)
            bundle(
                "all",
                listOf("stdlib", "stdlib-common")
            )
        }

        create("coroutinesLibs") {
            val versionCoroutines = "1.5.2"

            alias("core").to("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
                .version(versionCoroutines)
            alias("android").to("org.jetbrains.kotlinx", "kotlinx-coroutines-android")
                .version(versionCoroutines)
            bundle(
                "all",
                listOf("core", "android")
            )
        }

        create("androidxLibs") {
            val versionLifecycle = "2.4.0"
            val versionDatastore = "1.0.0"
            val versionSqlite = "2.2.0-beta01"

            alias("core-ktx").to("androidx.core", "core-ktx")
                .version("1.7.0")
            alias("activity-ktx").to("androidx.activity", "activity-ktx")
                .version("1.4.0")
            alias("fragment-ktx").to("androidx.fragment", "fragment-ktx")
                .version("1.4.0-rc01")
            alias("appcompat").to("androidx.appcompat", "appcompat")
                .version("1.4.0-rc01")
            bundle(
                "common",
                listOf("core-ktx", "activity-ktx", "fragment-ktx", "appcompat")
            )

            alias("lifecycle-viewmodel-ktx").to("androidx.lifecycle", "lifecycle-viewmodel-ktx")
                .version(versionLifecycle)
            alias("lifecycle-viewmodel-savedstate").to("androidx.lifecycle", "lifecycle-viewmodel-savedstate")
                .version(versionLifecycle)
            alias("lifecycle-runtime-ktx").to("androidx.lifecycle", "lifecycle-runtime-ktx")
                .version(versionLifecycle)
            alias("lifecycle-livedata-ktx").to("androidx.lifecycle", "lifecycle-livedata-ktx")
                .version(versionLifecycle)
            bundle(
                "lifecycle",
                listOf(
                    "lifecycle-viewmodel-ktx",
                    "lifecycle-viewmodel-savedstate",
                    "lifecycle-runtime-ktx",
                    "lifecycle-livedata-ktx"
                )
            )

            alias("recyclerview").to("androidx.recyclerview", "recyclerview")
                .version("1.3.0-alpha01")
            alias("constraintlayout").to("androidx.constraintlayout", "constraintlayout")
                .version("2.1.1")
            alias("swiperefreshlayout").to("androidx.swiperefreshlayout", "swiperefreshlayout")
                .version("1.2.0-alpha01")
            bundle(
                "view",
                listOf("recyclerview", "constraintlayout", "swiperefreshlayout")
            )

            alias("annotation").to("androidx.annotation", "annotation")
                .version("1.3.0")
            alias("setup").to("androidx.startup", "startup-runtime")
                .version("1.1.0")

            alias("datastore-core").to("androidx.datastore", "datastore-core")
                .version(versionDatastore)
            alias("datastore-preferences").to("androidx.datastore", "datastore-preferences")
                .version(versionDatastore)
            bundle(
                "datastore",
                listOf("datastore-core", "datastore-preferences")
            )

            alias("sqlite-ktx").to("androidx.sqlite", "sqlite-ktx")
                .version(versionSqlite)
            alias("sqlite-framework").to("androidx.sqlite", "sqlite-framework")
                .version(versionSqlite)
            bundle(
                "sqlite",
                listOf("sqlite-ktx", "sqlite-framework")
            )

            alias("multidex").to("androidx.multidex", "multidex")
                .version("2.0.1")
        }

        create("composeLibs") {
            val versionCompose = "1.1.0-beta02"
            val versionAccompanist = "0.21.2-beta"

            version("compose", versionCompose)

            alias("ui").to("androidx.compose.ui", "ui")
                .version(versionCompose)
            alias("tooling").to("androidx.compose.ui", "ui-tooling")
                .version(versionCompose)
            alias("runtime").to("androidx.compose.runtime", "runtime")
                .version(versionCompose)
            alias("livedata").to("androidx.compose.runtime", "runtime-livedata")
                .version(versionCompose)
            alias("foundation").to("androidx.compose.foundation", "foundation")
                .version(versionCompose)
            alias("foundation-layout").to("androidx.compose.foundation", "foundation-layout")
                .version(versionCompose)
            alias("animation").to("androidx.compose.animation", "animation")
                .version(versionCompose)

            alias("material3").to("androidx.compose.material3", "material3")
                .version("1.0.0-alpha01")
            alias("material").to("androidx.compose.material", "material")
                .version("1.0.0-alpha01")
            alias("activity").to("androidx.activity", "activity-compose")
                .version("1.4.0")
            bundle(
                "common",
                listOf(
                    "ui",
                    "tooling",
                    "runtime",
                    "livedata",
                    "foundation",
                    "foundation-layout",
                    "animation",
                    "material3",
                    "material",
                    "activity"
                )
            )

            alias("accompanist-swiperefresh").to("com.google.accompanist", "accompanist-swiperefresh")
                .version(versionAccompanist)
            alias("accompanist-insets").to("com.google.accompanist", "accompanist-insets")
                .version(versionAccompanist)
            alias("accompanist-systemuicontroller").to("com.google.accompanist", "accompanist-systemuicontroller")
                .version(versionAccompanist)
            alias("accompanist-permissions").to("com.google.accompanist", "accompanist-permissions")
                .version(versionAccompanist)
            alias("accompanist-coil").to("com.google.accompanist", "accompanist-coil")
                .version("0.15.0")

            bundle(
                "accompanist",
                listOf(
                    "accompanist-swiperefresh",
                    "accompanist-insets",
                    "accompanist-systemuicontroller",
                    "accompanist-permissions",
                    "accompanist-coil"
                )
            )

            alias("viewmodel").to("androidx.lifecycle", "lifecycle-viewmodel-compose")
                .version("2.4.0")

            alias("navigation").to("androidx.navigation", "navigation-compose")
                .version("2.4.0-beta02")

            alias("window").to("androidx.window", "window")
                .version("1.0.0-beta03")
        }

        create("okhttpLibs") {
            val versionOkHttp = "4.9.2"
            alias("okhttp").to("com.squareup.okhttp3", "okhttp")
                .version(versionOkHttp)
            alias("logging").to("com.squareup.okhttp3", "logging-interceptor")
                .version(versionOkHttp)
            bundle(
                "all",
                listOf("okhttp", "logging")
            )
        }

        create("googleLibs") {
            alias("material").to("com.google.android.material", "material")
                .version("1.5.0-alpha05")
            alias("gson").to("com.google.code.gson", "gson")
                .version("2.8.9")
        }

        create("imageLibs") {
            alias("coil").to("io.coil-kt", "coil")
                .version("2.0.0-alpha02")
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
    ":kot-gson",
    ":reader"
)