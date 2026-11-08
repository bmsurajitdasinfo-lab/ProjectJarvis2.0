plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.projectjarvis"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.projectjarvis"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }
}
