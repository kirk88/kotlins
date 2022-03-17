plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    compileSdk = BuildVersions.compileSdk

    defaultConfig {
        minSdk = BuildVersions.minSdk
        targetSdk = BuildVersions.targetSdk
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
        sourceCompatibility = JavaVersions.sourceCompatibility
        targetCompatibility = JavaVersions.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = KotlinOptions.jvmTarget
        freeCompilerArgs = KotlinOptions.compilerArgs
    }

    sourceSets {
        named("main") {
            java.srcDir("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(kotlinLibs.bundles.all)
    implementation(coroutinesLibs.bundles.all)
    implementation(androidxLibs.setup)
    implementation(androidxLibs.annotation)
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 6

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.nice.kotlins"
                artifactId = "kot-bluetooth"
                version = "${versionMajor}.${versionMinor}.${versionPatch}"

                artifact(sourcesJar)
                artifact(tasks.getByName("bundleReleaseAar"))

                pom {
                    name.set("kot-bluetooth")
                    description.set("Kotlin Asynchronous Bluetooth Low-Energy")
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
