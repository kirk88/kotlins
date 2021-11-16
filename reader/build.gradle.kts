plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = app.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.hao.reader"
        minSdk = app.versions.minSdk.get().toInt()
        targetSdk = app.versions.targetSdk.get().toInt()
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
        jvmTarget = kotlinLibs.versions.jvmTarget.get()
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
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
    implementation(project(":kot-sqlite"))
    implementation(project(":kot-okhttp"))
    implementation(project(":kot-gson"))
}