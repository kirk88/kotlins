plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = BuildVersions.compileSdk

    defaultConfig {
        applicationId = "com.hao.reader"
        minSdk = BuildVersions.minSdk
        targetSdk = BuildVersions.targetSdk
        versionCode = BuildVersions.versionCode
        versionName = BuildVersions.versionName

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
        sourceCompatibility = JavaVersions.sourceCompatibility
        targetCompatibility = JavaVersions.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = KotlinOptions.jvmTarget
        freeCompilerArgs = KotlinOptions.compilerArgs
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