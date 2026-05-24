import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

// Resolve signing config in this order:
//   1. env vars (CI: FINBABY_STORE_FILE, FINBABY_STORE_PASSWORD, FINBABY_KEY_ALIAS, FINBABY_KEY_PASSWORD)
//   2. keystore.properties in project root (local dev)
// If neither is present, release builds will be unsigned (debug builds still work).
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
fun signingValue(envName: String, propName: String): String? =
    System.getenv(envName)?.takeIf { it.isNotBlank() } ?: keystoreProps.getProperty(propName)

val storeFilePath = signingValue("FINBABY_STORE_FILE", "storeFile")
val storePassword = signingValue("FINBABY_STORE_PASSWORD", "storePassword")
val keyAlias = signingValue("FINBABY_KEY_ALIAS", "keyAlias")
val keyPassword = signingValue("FINBABY_KEY_PASSWORD", "keyPassword")
val hasReleaseSigning = listOf(storeFilePath, storePassword, keyAlias, keyPassword).all { !it.isNullOrBlank() }

android {
    namespace = "com.finbaby.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.finbaby.app"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("FINBABY_VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("FINBABY_VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(storeFilePath!!)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Debug uses the auto-generated debug keystore.
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("com.google.dagger:hilt-compiler:2.54")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.2")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Biometric
    implementation("androidx.biometric:biometric:1.1.0")

    // CSV Export
    implementation("com.opencsv:opencsv:5.9")

    // Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.6")

    // Datastore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Gson for backup/restore
    implementation("com.google.code.gson:gson:2.11.0")
}
