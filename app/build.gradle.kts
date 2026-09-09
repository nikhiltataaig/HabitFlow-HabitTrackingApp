
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.habitflow"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.habitflow"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        //isCoreLibraryDesugaringEnabled = true

    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform(libs.firebase.bom.v3450))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.runtime.ktx)

    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)




    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.places)

    ksp(libs.androidx.room.compiler)

     implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // -------------------------------------------------------------------------
    // Compose
    // -------------------------------------------------------------------------

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

   // -------------------------------------------------------------------------
    // Coil
    // -------------------------------------------------------------------------

    implementation(libs.coil.compose.v330)
    implementation(libs.coil.compose)

    // -------------------------------------------------------------------------
    // AndroidX
    // -------------------------------------------------------------------------

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // -------------------------------------------------------------------------
    // Hilt
    // -------------------------------------------------------------------------

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // -------------------------------------------------------------------------
    // Firebase
    // -------------------------------------------------------------------------

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // -------------------------------------------------------------------------
    // AVIF / Glide
    // -------------------------------------------------------------------------

    implementation(libs.avif.integration)
    implementation(libs.glide)

    // -------------------------------------------------------------------------
    // Kotlin Coroutines
    // -------------------------------------------------------------------------

    implementation(libs.kotlinx.coroutines.android)

    // -------------------------------------------------------------------------
    // Kotlin Serialization
    // -------------------------------------------------------------------------

    // Your serialization dependency is already provided by your version catalog
    // through the Kotlin serialization plugin.

    // -------------------------------------------------------------------------
    // Unit tests
    // -------------------------------------------------------------------------

    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)
    testImplementation(libs.kotlinx.coroutines.test)

    // -------------------------------------------------------------------------
    // Android instrumented tests
    // -------------------------------------------------------------------------

    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.10.0")

    // -------------------------------------------------------------------------
    // Debug test dependencies
    // -------------------------------------------------------------------------

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)


}