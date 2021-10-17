plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    compileSdk = androids.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = androids.versions.minSdk.get().toInt()
        targetSdk = androids.versions.targetSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(libs.google.gson)
    api(libs.bundles.okhttp3)
    api(libs.bundles.kotlinx.coroutines)
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

tasks {
    register<com.android.build.gradle.tasks.SourceJarTask>("sourcesJar") {
        variantName = "sources"
        archiveClassifier.set("sources")
    }

    register<com.android.build.gradle.tasks.JavaDocJarTask>("javadocJar") {
        variantName = "javadoc"
        archiveClassifier.set("javadoc")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("kothttp") {
                groupId = "com.nice.kotlins"
                artifactId = "kothttp"
                version = "${versionMajor}.${versionMinor}.${versionPatch}"

                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))
                artifact(tasks.getByName("bundleReleaseAar"))

                pom {
                    name.set("kothttp")
                    description.set("New okhttp api in kotlin")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")
                        configurations.implementation.get().allDependencies.forEach { dependency ->
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dependency.group)
                            dependencyNode.appendNode("artifactId", dependency.name)
                            dependencyNode.appendNode("version", dependency.version)
                        }
                    }
                }
            }
        }
        repositories {
            val deployPath = file(requireNotNull(properties["libs.deployPath"]))
            maven("file://${deployPath.absolutePath}")
        }
    }
}