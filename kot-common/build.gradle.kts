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

    buildFeatures {
        viewBinding = true
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
    api(coroutinesLibs.bundles.all)
    api(androidxLibs.bundles.common)
    api(androidxLibs.bundles.view)
    api(androidxLibs.bundles.lifecycle)
    api(androidxLibs.bundles.datastore)
    api(androidxLibs.setup)
    api(androidxLibs.multidex)
    api(googleLibs.material)
    api(imageLibs.coil)
}

val versionMajor = 1
val versionMinor = 2
val versionPatch = 0
val versionSuffix = "alpha09"

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.nice.kotlins"
                artifactId = "kot-common"
                version = "${versionMajor}.${versionMinor}.${versionPatch}-${versionSuffix}"

                artifact(sourcesJar)
                artifact(tasks.getByName("bundleReleaseAar"))

                pom {
                    name.set("kot-common")
                    description.set("A sweet set of kotlin common tools")
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