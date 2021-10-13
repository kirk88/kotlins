enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.github.ben-manes.versions") version "0.39.0"
//        id("com.android.library") version "7.0.3"
//        id("kotlin-android") version "1.5.31"
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
            alias("android").toPluginId("com.android.library").version("7.0.3")
        }
    }
}

rootProject.name = "kotlins"
rootProject.buildFileName = "build.gradle.kts.kts"
//include(":sample")
//include(":common")
//include(":bluetooth")
//include(":sqlite")
//include(":kothttp")
//include(":kotson")
include(":atomic")

//ext.versions = [
//    compileSdk       : 31,
//minSdk           : 19,
//targetSdk        : 30,
//
//pluginVersions   : "0.39.0",
//pluginAndroid    : '7.0.3',
//pluginKotlin     : "1.5.31",
//
//kotlinStdlib     : "1.5.31",
//
//kotlinxCoroutines: "1.5.2",
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
//    versionsPlugin                    : "com.github.ben-manes:gradle-versions-plugin:${versions.pluginVersions}",
//
//androidPlugin                     : "com.android.tools.build:gradle:${versions.pluginAndroid}",
//kotlinPlugin                      : "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.pluginKotlin}",
//
//kotlinStdlib                      : "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlinStdlib}",
//
//kotlinxCoroutinesCore             : "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlinxCoroutines}",
//kotlinxCoroutinesAndroid          : "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.kotlinxCoroutines}",
//
//kotlinLifecycleViewmodelKtx       : "androidx.lifecycle:lifecycle-viewmodel-ktx:${versions.kotlinLifecycle}",
//kotlinLifecycleViewmodelSavedstate: "androidx.lifecycle:lifecycle-viewmodel-savedstate:${versions.kotlinLifecycle}",
//kotlinLifecycleRuntime            : "androidx.lifecycle:lifecycle-runtime-ktx:${versions.kotlinLifecycle}",
//kotlinLifecycleLivedata           : "androidx.lifecycle:lifecycle-livedata-ktx:${versions.kotlinLifecycle}",
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