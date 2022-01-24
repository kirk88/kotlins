import java.util.logging.Logger.global

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.toVersion(vers.versions.javaVersion.get())
    targetCompatibility = JavaVersion.toVersion(vers.versions.javaVersion.get())

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = vers.versions.jvmTarget.get()
        }
    }
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 3

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nice.kotlins"
            artifactId = "kot-atomic"
            version = "${versionMajor}.${versionMinor}.${versionPatch}"

            from(components["java"])

            pom {
                name.set("kot-atomic")
                description.set("Kotlin atomic operations")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

