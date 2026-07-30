import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Secrets and per-machine settings live in local.properties, which is
// gitignored, so nothing here ends up in the repository. Copy
// local.properties.example to local.properties and fill it in.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(name: String, fallback: String = ""): String =
    (localProps.getProperty(name) ?: System.getenv(name) ?: fallback)

android {
    namespace = "com.workwise"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.workwise"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 10.0.2.2 is how the Android emulator reaches localhost on the host
        // machine, so a fresh clone points at a backend you run yourself.
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${secret("apiBaseUrl", "http://10.0.2.2:8000/")}\""
        )
        buildConfigField(
            "String",
            "API_TOKEN",
            "\"${secret("apiToken")}\""
        )

        // Supplied to the manifest and to res/values, so the key is never
        // committed. Without one the app still runs; only the map stays blank.
        resValue("string", "google_maps_key", secret("mapsApiKey"))
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.7.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.camera.camera2.pipe)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
