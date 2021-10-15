plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":kotorm"))
    implementation(project(":kotble"))
    implementation(project(":kothttp"))
    implementation(project(":atomic"))
}