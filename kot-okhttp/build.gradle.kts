plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    compileSdk = app.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = app.versions.minSdk.get().toInt()
        targetSdk = app.versions.targetSdk.get().toInt()
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
        jvmTarget = kotlinLibs.versions.jvmTarget.get()
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    sourceSets {
        named("main") {
            java.srcDir("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(coroutinesLibs.bundles.all)
    implementation(googleLibs.gson)
    api(okhttpLibs.bundles.all)
}

val versionMajor = 1
val versionMinor = 1
val versionPatch = 1

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.nice.kotlins"
                artifactId = "kot-okhttp"
                version = "${versionMajor}.${versionMinor}.${versionPatch}"

                artifact(sourcesJar)
                artifact(tasks.getByName("bundleReleaseAar"))

                pom {
                    name.set("kot-okhttp")
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