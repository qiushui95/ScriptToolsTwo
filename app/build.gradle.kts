plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.tools.script.two"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tools.script.two"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {

            keyAlias = "RedFinger"
            keyPassword = "redfinger"
            storeFile = File(rootProject.projectDir, "keys/red_finger.jks")
            storePassword = "redfinger"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.foundation)
    implementation(libs.compose.graphics)
    implementation(libs.compose.material)
    implementation(libs.compose.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)

    implementation(libs.flowmvi.core)
    implementation(libs.flowmvi.compose)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.androidUtils)

    implementation(libs.xpopup)

    implementation(libs.lifecycle.service)

    implementation(libs.jodatime)
}