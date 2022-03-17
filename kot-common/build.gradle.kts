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
    api(Libs.KotlinStdlib)
    api(Libs.KotlinStdlibCommon)
    api(Libs.CoroutinesCore)
    api(Libs.CoroutinesAndroid)
    api(Libs.CoreKtx)
    api(Libs.ActivityKtx)
    api(Libs.FragmentKtx)
    api(Libs.Appcompat)
    api(Libs.LifecycleRuntimeKtx)
    api(Libs.LifecycleViewModelSavedState)
    api(Libs.LifecycleViewModelKtx)
    api(Libs.DataStoreCore)
    api(Libs.DataStorePreferences)
    api(Libs.Startup)
    api(Libs.Multidex)
    api(Libs.Material)
    api(Libs.Coil)
}

val versionMajor = 1
val versionMinor = 2
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
                artifactId = "kot-common"
                version = "${versionMajor}.${versionMinor}.${versionPatch}"

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