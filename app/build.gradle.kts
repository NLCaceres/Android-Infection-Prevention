plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = false
    namespace = "edu.usc.nlcaceres.infectionprevention"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    val composeBom = platform("androidx.compose:compose-bom:2023.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Basic Android Dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1") // Android seems to preload the Java versions
    implementation("androidx.fragment:fragment-ktx:1.6.0") // SO need to add "-ktx" for the Kotlin versions!
    val lifecycle_version = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // lifecycle-viewModel gets its needed viewModels() delegate from androidx.activity transitively thru .fragment
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // Basic Android View Dependencies
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // Seems to transitively get coroutines-core & its Dispatchers.Main
    // Jetpack Compose Dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.compose.material:material") // Needed for AppCompatTheme to work
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview") // Preview support
    debugImplementation("androidx.compose.ui:ui-tooling") // Preview support
    // Google Accompanist Dependencies
    val accompanist_version = "0.30.1"
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanist_version")
    implementation("com.google.accompanist:accompanist-themeadapter-appcompat:$accompanist_version")
    implementation("com.google.accompanist:accompanist-themeadapter-material:$accompanist_version")
    // Navigation Graph Dependencies
    val nav_version = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Google & Square Dependencies
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")
    val hilt_version = "2.46.1"
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    // Test Dependencies - Unit Test Specific
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0") // Helps with liveData in tests
    val espresso_version = "3.5.1"
    testImplementation("androidx.test.espresso:espresso-core:$espresso_version") // For robolectric UI-Unit testing
    testImplementation("org.robolectric:robolectric:4.10.3")
    implementation("com.google.accompanist:accompanist-testharness:$accompanist_version")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1") // Mocks out Dispatcher.Main for coroutine/suspended funs

    // Instrumented Test Specific (Emulator based)
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
    androidTestImplementation("androidx.test.espresso:espresso-intents:$espresso_version")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espresso_version")
    implementation("androidx.test.espresso:espresso-idling-resource:$espresso_version") // To insert EspressoTestIdler into debug versions of app files
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hilt_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
repositories {
    mavenCentral()
}
