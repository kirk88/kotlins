plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 2

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nice.kotlins"
            artifactId = "kot-atomic"
            version = "${versionMajor}.${versionMinor}.${versionPatch}"

            from(components["java"])

            pom {
                name.set("atomic")
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

