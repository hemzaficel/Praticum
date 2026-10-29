plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.praticum"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.praticum"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    //implementation("com.google.android.material:material:1.12.0")
    //implementation("androidx.appcompat:appcompat:1.7.0")
    //implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("androidx.navigation:navigation-fragment:2.8.5")
    implementation("androidx.navigation:navigation-ui:2.8.5")


    // Google ML Kit : détection des objets
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")


    // === AJOUTEZ ROOM ICI ===
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

}