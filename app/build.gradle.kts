plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "app.nextstep.offline"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.nextstep.offline"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
