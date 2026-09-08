plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.9" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false

}