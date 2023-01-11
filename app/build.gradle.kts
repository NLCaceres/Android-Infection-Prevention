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
    buildFeatures.viewBinding = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.animationsDisabled = false
    namespace = "edu.usc.nlcaceres.infectionprevention"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    // Basic Android Dependencies
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.core:core-ktx:1.9.0") // AndroidStudio has the preloaded Java versions
    implementation("androidx.fragment:fragment-ktx:1.5.5") // SO need to add "-ktx" Kotlin versions!
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    // lifecycle-viewModel gets its needed viewModels() delegate from androidx.activity transitively thru .fragment
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    // Basic Android View Dependencies
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4") // Seems to transitively get coroutines-core & its Dispatchers.Main
    // Navigation Graph Dependencies
    val nav_version = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Google & Square Dependencies
    implementation("com.google.android.material:material:1.7.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")

    // Test Dependencies - Unit Test Specific
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.1.0") // Helps with liveData in tests
    testImplementation("androidx.test.espresso:espresso-core:3.5.1") // For robolectric UI-Unit testing
    testImplementation("org.robolectric:robolectric:4.9")
    testImplementation("org.mockito:mockito-inline:4.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4") // Mocks out Dispatcher.Main for coroutine/suspended funs

    // Instrumented Test Specific (Emulator based)
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    implementation("androidx.test.espresso:espresso-idling-resource:3.5.1") // To insert EspressoTestIdler into debug versions of app files
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.43.2")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.43.2")
}
repositories {
    mavenCentral()
}
