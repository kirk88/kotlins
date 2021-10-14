plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(libs.google.gson)
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("kotson") {
                groupId = "com.nice.kotlins"
                artifactId = "kotson"
                version = "${versionMajor}.${versionMinor}.${versionPatch}"

                from(components["java"])

                pom {
                    name.set("kotson")
                    description.set("Kotlin extensions for JSON manipulation via Gson")
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

