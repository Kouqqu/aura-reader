plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.aura.reader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aura.reader"
        minSdk = 26
        targetSdk = 35
        versionCode = 44
        versionName = "1.4.2-beta.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "androiddebugkey"
            keyAlias = "androiddebugkey"
            keyPassword = "androiddebugkey"
        }
        create("release") {
            val ksPath = System.getenv("SIGNING_KEY_STORE_PATH")
                ?: project.findProperty("RELEASE_STORE_FILE") as String?
                ?: "aura-release.jks"
            val storePass = System.getenv("SIGNING_STORE_PASSWORD")
                ?: project.findProperty("RELEASE_STORE_PASSWORD") as String?
                ?: "aura"
            val alias = System.getenv("SIGNING_KEY_ALIAS")
                ?: project.findProperty("RELEASE_KEY_ALIAS") as String?
                ?: "aura"
            val keyPass = System.getenv("SIGNING_KEY_PASSWORD")
                ?: project.findProperty("RELEASE_KEY_PASSWORD") as String?
                ?: "aura"

            val ksFile = sequenceOf(file(ksPath), rootProject.file(ksPath)).firstOrNull { it.exists() } ?: file(ksPath)
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            } else {
                storeFile = file("debug.keystore")
                storePassword = "androiddebugkey"
                keyAlias = "androiddebugkey"
                keyPassword = "androiddebugkey"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.compose.ui:ui-text-google-fonts")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.jsoup)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.androidx.documentfile)
    implementation("androidx.palette:palette-ktx:1.0.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
}
