plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
}

val sourceJar by tasks.creating(Jar::class) {
    archiveClassifier.set("source")
    from(sourceSets["main"].allJava)
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

            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }

            artifact(sourceJar)

            pom {
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
        val deployPath = file(requireNotNull(properties["aar.deployPath"]))
        maven { url = uri("file://${deployPath.absolutePath}") }
    }
}