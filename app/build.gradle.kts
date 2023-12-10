@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
}

android {
    namespace = "edu.usc.nlcaceres.infectionprevention"
    compileSdk = 33
    defaultConfig {
        applicationId = "edu.usc.nlcaceres.infectionprevention"
        minSdk = 26 // Android 8.0 Oreo
        targetSdk = 33 // Robolectric can't handle 33 yet
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "edu.usc.nlcaceres.infectionprevention.HiltTestRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") { // Not needed since usually automagically made BUT if you want to override it, must include "debuggable true"
            applicationIdSuffix = ".debug"
            isDebuggable = true // Only needed in debug
            // minSdk = 28 // 28 for debug since InstrumentedTests runs mocks where dexmaker needs a higher version
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = false
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Basic Android Dependencies
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx) // Android seems to preload the Java versions
    implementation(libs.androidx.fragment.ktx) // SO need to add "-ktx" for the Kotlin versions!
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // lifecycle-viewModel gets its needed viewModels() delegate from androidx.activity transitively thru .fragment
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Basic Android View Dependencies
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preferences.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.kotlinx.coroutines.android) // Seems to transitively get coroutines-core & its Dispatchers.Main
    // Jetpack Compose Dependencies
    implementation(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling) // Preview support
    implementation(libs.androidx.compose.ui.tooling.preview) // Preview support
    implementation(libs.androidx.compose.material) // Needed for AppCompatTheme to work
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    // Google Accompanist Dependencies
    implementation(libs.accompanist.themeadapter.appcompat)
    implementation(libs.accompanist.themeadapter.material)
    implementation(libs.accompanist.placeholder.material)
    // Navigation Graph Dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Google & Square Dependencies
    implementation(libs.google.android.material)
    implementation(libs.google.android.flexbox)
    implementation(libs.google.code.gson)
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.google.dagger.hilt.compiler)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    debugImplementation(libs.squareup.leakcanary)

    // Test Dependencies - Unit Test Specific
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit.ktx)
    testImplementation(libs.androidx.arch.core.testing) // Helps with liveData in tests
    testImplementation(libs.androidx.test.espresso.core) // For robolectric UI-Unit testing
    testImplementation(libs.robolectric)
    implementation(libs.accompanist.testharness)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test) // Mocks out Dispatcher.Main for coroutine/suspended funs

    // Instrumented Test Specific (Emulator based)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    implementation(libs.androidx.test.espresso.idling.resource) // To insert EspressoTestIdler into debug versions of app files
    androidTestImplementation(libs.google.dagger.hilt.android.testing)
    kaptAndroidTest(libs.google.dagger.hilt.android.compiler)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
