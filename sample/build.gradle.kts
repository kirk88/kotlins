plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = androids.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.sample"
        minSdk = androids.versions.minSdk.get().toInt()
        targetSdk = androids.versions.targetSdk.get().toInt()
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composes.versions.compose.get()
    }

}

dependencies {
    implementation(project(":kot-sqlite"))
    implementation(project(":kot-bluetooth"))
    implementation(project(":kot-okhttp"))
    implementation(project(":kot-gson"))
    implementation(composes.bundles.common)
    implementation(composes.bundles.accompanist)
    implementation(composes.viewmodel)
    implementation(composes.navigation)
    implementation(composes.window)
}