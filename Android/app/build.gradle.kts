import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Real OAuth client IDs/secrets are developer-machine-local config, never
// committed (see local.properties.example) — mirrors how sdk.dir is handled.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}

android {
    namespace = "com.diveapp.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.diveapp.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties.getProperty("NAVER_CLIENT_ID", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties.getProperty("NAVER_CLIENT_SECRET", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_NAME", "\"${localProperties.getProperty("NAVER_CLIENT_NAME", "DiveApp")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")

        // AndroidManifest.xml cannot read BuildConfig, so the NAVER Maps key
        // is injected as a manifest placeholder instead of a buildConfigField.
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] = localProperties.getProperty("NAVER_MAP_CLIENT_ID", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Naver Login SDK — issues a Naver OAuth access token (see NidOAuth calls
    // in core/auth/SocialAuthProvider.kt). NaverIdLoginSDK is deprecated in
    // favor of NidOAuth as of this version.
    implementation("com.navercorp.nid:oauth:5.11.2")

    // Google Sign-In via Credential Manager — issues a Google ID token
    // verified server-side against GOOGLE_WEB_CLIENT_ID (a Web-application
    // type OAuth client, per Google's own architecture — used even here on
    // Android so the backend's single GOOGLE_CLIENT_ID setting works for
    // both Web and Android).
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")

    // NAVER Maps Android SDK — dive log location picker (map + marker only,
    // no place search/reverse geocoding in the SDK per Docs/12).
    implementation("com.naver.maps:map-sdk:3.23.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
