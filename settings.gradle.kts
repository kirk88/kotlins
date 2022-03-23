pluginManagement {
    repositories {
        google { url = uri("https://maven.aliyun.com/repository/google") }
        mavenCentral { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
    plugins {
        id("com.android.application") version "7.1.2"
        id("com.android.library") version "7.1.2"
        id("org.jetbrains.kotlin.android") version "1.6.10"
        id("org.jetbrains.kotlin.jvm") version "1.6.10"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google { url = uri("https://maven.aliyun.com/repository/google") }
        mavenCentral { url = uri("https://maven.aliyun.com/repository/central") }
    }
}
rootProject.name = "kotlins"
rootProject.buildFileName = "build.gradle.kts"
include(":kot-atomic")
include(":kot-bluetooth")
include(":kot-common")
include(":kot-sqlite")
include(":kot-okhttp")
include(":kot-gson")
include(":app")