plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id ("androidx.navigation.safeargs")
    id ("kotlin-parcelize")

}

android {
    namespace = "com.example.mvvm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mvvm"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("org.osmdroid:osmdroid-android:6.1.10")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.play.services.location)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.2")

    kapt ("androidx.room:room-compiler:2.6.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    // ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // Check for latest version
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // Check for latest version

    // Coroutine support for ViewModel
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Check for latest version

    implementation ("org.osmdroid:osmdroid-android:6.1.10")

    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

    // Dependencies for local unit tests
    // Unit Testing Libraries
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.hamcrest:hamcrest-all:1.3")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("org.robolectric:robolectric:4.10.3")

    // AndroidX Test - JVM testing
    testImplementation ("androidx.test:core-ktx:1.5.0")

    // AndroidX Test - Instrumented testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // Timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    // Hamcrest
    testImplementation ("org.hamcrest:hamcrest:2.2")
    testImplementation ("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation ("org.hamcrest:hamcrest:2.2")
    androidTestImplementation ("org.hamcrest:hamcrest-library:2.2")

    // AndroidX Test and Robolectric
    testImplementation ("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation ("androidx.test:core-ktx:1.5.0")

    // InstantTaskExecutorRule
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")

    // Kotlin Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}