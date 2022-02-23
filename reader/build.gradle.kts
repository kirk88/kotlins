plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = vers.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.hao.reader"
        minSdk = vers.versions.minSdk.get().toInt()
        targetSdk = vers.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(vers.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(vers.versions.javaVersion.get())
    }

    kotlinOptions {
        jvmTarget = vers.versions.jvmTarget.get()
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeLibs.versions.compose.get()
    }

}

dependencies {
    implementation(composeLibs.bundles.common)
    implementation(composeLibs.bundles.accompanist)
    implementation(composeLibs.viewmodel)
    implementation(composeLibs.navigation)
    implementation(composeLibs.window)
    implementation(project(":kot-bluetooth"))
    implementation(project(":kot-sqlite"))
    implementation(project(":kot-okhttp"))
    implementation(project(":kot-gson"))
}