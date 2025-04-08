plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Plugin cho Google Services (Firebase)
}

android {
    namespace = "com.example.thue_tro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.thue_tro"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Material Design Components
    implementation(libs.material) // Đảm bảo Material3 từ libs.gradle
    implementation("com.google.android.material:material:1.12.0") // Phiên bản cụ thể

    // Firebase (Sử dụng BOM để đồng bộ phiên bản)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2")) // Cập nhật phiên bản mới nhất
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation("com.google.firebase:firebase-database") // Realtime Database
    implementation("com.google.firebase:firebase-storage") // Firebase Storage
    implementation("com.google.firebase:firebase-firestore") // Firestore (nếu cần)

    // Glide (chỉ cần một phiên bản, chọn mới nhất)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0") // Loại bỏ trùng lặp

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}