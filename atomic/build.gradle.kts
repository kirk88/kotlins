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
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

publishing {
    publications {
        create<MavenPublication>("atomic") {
            groupId = "com.nice.kotlins"
            artifactId = "atomic"
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

