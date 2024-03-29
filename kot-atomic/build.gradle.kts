plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersions.sourceCompatibility
    targetCompatibility = JavaVersions.targetCompatibility

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = KotlinVersions.jvmTarget
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

