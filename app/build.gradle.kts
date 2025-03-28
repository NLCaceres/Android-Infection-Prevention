plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "edu.usc.nlcaceres.infectionprevention"
    compileSdk = 35
    defaultConfig {
        applicationId = "edu.usc.nlcaceres.infectionprevention"
        minSdk = 26 // Android 8.0 Oreo
        targetSdk = 35
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
        buildConfig = true
        viewBinding = true
        compose = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true // Helps add Java 8 & Java 11 APIs previously unavailable
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = false
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    coreLibraryDesugaring(libs.android.tools.desugar)
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
    implementation(libs.androidx.compose.ui.tooling.preview) // Preview support
    debugImplementation(libs.androidx.compose.ui.tooling) // Preview support
    implementation(libs.androidx.compose.material3) // Transitively gets `.compose.ui` aka the main Compose APIs
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.runtime.compose) // Lets Kotlin Stateflows work with Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    // Navigation Graph Dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Google & Square Dependencies
    implementation(libs.google.android.material) // Provide Material 3 XML Views + Theming
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
    androidTestImplementation(libs.kaspersky.kaspresso)
    androidTestImplementation(libs.kaspersky.kaspresso.compose)
}

kapt {
    correctErrorTypes = true
}
