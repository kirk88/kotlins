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

    kapt.includeCompileClasspath = false

}

dependencies {
    implementation(project(":kot-common"))
    implementation(project(":kot-sqlite"))
    implementation(project(":kot-bluetooth"))
    implementation(project(":kot-okhttp"))
    implementation(project(":kot-atomic"))
    implementation(project(":kot-gson"))

    val room_version = "2.4.0-beta01"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
}