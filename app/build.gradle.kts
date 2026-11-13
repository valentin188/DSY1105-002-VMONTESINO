plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.modoguardian"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.modoguardian"

        minSdk = 33
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    val composeBom = platform(
        "androidx.compose:compose-bom:2024.06.00"
    )

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(
        "androidx.activity:activity-compose:1.9.0"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-graphics"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.material:material-icons-extended:1.6.8"
    )

    implementation(
        "androidx.compose.material3:material3-window-size-class:1.2.1"
    )

    implementation(
        "androidx.core:core-ktx:1.13.1"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.8.2"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.8.2"
    )

    implementation(
        "androidx.navigation:navigation-compose:2.7.7"
    )

    implementation(
        "com.squareup.retrofit2:retrofit:2.11.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:2.11.0"
    )

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0"
    )

    val roomVersion = "2.8.5"

    implementation(
        "androidx.room:room-runtime:$roomVersion"
    )

    implementation(
        "androidx.room:room-ktx:$roomVersion"
    )

    ksp(
        "androidx.room:room-compiler:$roomVersion"
    )

    implementation(
        "androidx.datastore:datastore-preferences:1.1.1"
    )

    implementation(
        "io.coil-kt:coil-compose:2.6.0"
    )

    testImplementation(
        "io.kotest:kotest-runner-junit5:5.8.6"
    )

    testImplementation(
        "io.kotest:kotest-assertions-core:5.8.8"
    )

    testImplementation(
        "org.junit.jupiter:junit-jupiter:5.10.6"
    )

    testImplementation(
        "io.mockk:mockk:1.13.10"
    )

    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0"
    )

    testImplementation(libs.junit)

    androidTestImplementation(
        "androidx.compose.ui:ui-test-junit4"
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    debugImplementation(
        "androidx.compose.ui:ui-test-manifest"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
