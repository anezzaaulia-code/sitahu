plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "anezza.aulia.sitahu"
    compileSdk = 36

    defaultConfig {
        applicationId = "anezza.aulia.sitahu"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
}