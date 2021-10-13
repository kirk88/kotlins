plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk versions.compileSdk

    defaultConfig {
        minSdk versions.minSdk
        targetSdk versions.targetSdk
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets {
        main.java.srcDirs += "src/main/kotlin"
    }
}

dependencies {
    implementation()
}

tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}

task androidSourcesJar(type: Jar) {
    archiveClassifier.convention("sources")
    archiveClassifier.set("sources")
    from android.sourceSets.main.java.getSrcDirs()
}

def versionMajor = 1
def versionMinor = 0
def versionPatch = 0

publishing {
    publications {
        production(MavenPublication) {
            groupId "com.nice.kotlins"
            artifactId "atomic"
            version "${versionMajor}.${versionMinor}.${versionPatch}"

            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }

            artifact androidSourcesJar

            pom.withXml {
                def dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.allDependencies.each { dependency ->
                    def dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", dependency.group)
                    dependencyNode.appendNode("artifactId", dependency.name)
                    dependencyNode.appendNode("version", dependency.version)
                }
            }
        }
    }
    repositories {
        def deployPath = file(getProperty("aar.deployPath"))
        maven { url "file://${deployPath.absolutePath}" }
    }
}