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
        jvmTarget = KotlinVersions.jvmTarget
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

}

dependencies {
    implementation(project(":kot-common"))
    implementation(project(":kot-bluetooth"))
    implementation(project(":kot-sqlite"))
    implementation(project(":kot-okhttp"))
    implementation(project(":kot-gson"))
}