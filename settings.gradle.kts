enableFeaturePreview("VERSION_CATALOGS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.1.0-alpha13"
        id("com.android.library") version "7.1.0-alpha13"
        id("org.jetbrains.kotlin.android") version "1.5.30"
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
        create("libs"){
            version("compileSdk", "31")
            version("minSdk", "21")
            version("targetSdk", "30")

            alias("kotlin-stdlib").to("org.jetbrains.kotlin", "kotlin-stdlib").version("1.5.30")
            alias("kotlin-coroutines-core").to("org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.5.2")
            alias("kotlin-coroutines-android").to("org.jetbrains.kotlinx", "kotlinx-coroutines-android").version("1.5.2")

            //kotlinLifecycleViewmodelKtx       : "androidx.lifecycle:lifecycle-viewmodel-ktx:${versions.kotlinLifecycle}",
//kotlinLifecycleViewmodelSavedstate: "androidx.lifecycle:lifecycle-viewmodel-savedstate:${versions.kotlinLifecycle}",
//kotlinLifecycleRuntime            : "androidx.lifecycle:lifecycle-runtime-ktx:${versions.kotlinLifecycle}",
//kotlinLifecycleLivedata           : "androidx.lifecycle:lifecycle-livedata-ktx:${versions.kotlinLifecycle}",
//            alias("kotlinStdlib").to("org.jetbrains.kotlinx", "kotlinx-coroutines-android").version("1.5.2")
        }
    }
}
rootProject.name = "kotlins"
rootProject.buildFileName = "build.gradle.kts.kts"
include(":atomic")
//include(":sample")
//include(":common")
//include(":bluetooth")
//include(":sqlite")
//include(":kothttp")
//include(":kotson")
//

//kotlinLifecycle  : "2.4.0-rc01",
//
//coreKtx          : "1.7.0-beta02",
//activityKtx      : "1.4.0-beta01",
//fragmentKtx      : "1.4.0-alpha10",
//
//annotation       : "1.3.0-beta01",
//
//setup            : "1.1.0",
//
//sqlite           : "2.2.0-alpha02",
//
//appcompat        : "1.4.0-beta01",
//
//recyclerview     : "1.3.0-alpha01",
//constraintlayout : "2.1.1",
//refreshlayout    : "1.2.0-alpha01",
//
//datastore        : "1.0.0",
//
//multidex         : "2.0.1",
//
//material         : "1.5.0-alpha04",
//
//okhttp           : "4.9.2",
//
//gson             : "2.8.8",
//coil             : "1.4.0"
//]
//
//ext.deps = [





//
//androidxCoreKtx                   : "androidx.core:core-ktx:${versions.coreKtx}",
//
//androidxActivityKtx               : "androidx.activity:activity-ktx:${versions.activityKtx}",
//androidxFragmentKtx               : "androidx.fragment:fragment-ktx:${versions.fragmentKtx}",
//
//androidxAnnotation                : "androidx.annotation:annotation:${versions.annotation}",
//
//androidxSetup                     : "androidx.startup:startup-runtime:${versions.setup}",
//
//androidxAppcompat                 : "androidx.appcompat:appcompat:${versions.appcompat}",
//
//androidxRecyclerview              : "androidx.recyclerview:recyclerview:${versions.recyclerview}",
//androidxConstraintlayout          : "androidx.constraintlayout:constraintlayout:${versions.constraintlayout}",
//androidxRefreshLayout             : "androidx.swiperefreshlayout:swiperefreshlayout:${versions.refreshlayout}",
//
//androidxDatastorePreferences      : "androidx.datastore:datastore-preferences:${versions.datastore}",
//androidxDatastoreCore             : "androidx.datastore:datastore-core:${versions.datastore}",
//
//androidxMultidex                  : "androidx.multidex:multidex:${versions.multidex}",
//
//androidxSqliteKtx                 : "androidx.sqlite:sqlite-ktx:${versions.sqlite}",
//androidxSqliteFramework           : "androidx.sqlite:sqlite-framework:${versions.sqlite}",
//
//material                          : "com.google.android.material:material:${versions.material}",
//
//okhttp3                           : "com.squareup.okhttp3:okhttp:${versions.okhttp}",
//okhttp3Logging                    : "com.squareup.okhttp3:logging-interceptor:${versions.okhttp}",
//
//gson                              : "com.google.code.gson:gson:${versions.gson}",
//
//coil                              : "io.coil-kt:coil:${versions.coil}",
//]
include(":mylibrary")
