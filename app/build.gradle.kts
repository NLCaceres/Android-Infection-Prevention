plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "edu.usc.nlcaceres.infectionprevention"
        minSdk = 21
        targetSdk = 32 // Robolectric can't handle 33 yet
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "edu.usc.nlcaceres.infectionprevention.HiltTestRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // minSdk = 21 // 21 for release
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

    // Test Dependencies - Unit Test Specific
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("org.robolectric:robolectric:4.8.1")
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4") // Mocks out Dispatcher.Main for coroutines/suspended funs

    // Instrumented Test Specific (Emulator based)
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.43.2")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.43.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0") // 3.4.0 causes hamcrest dependency mismatch currently
    implementation("androidx.test.espresso:espresso-idling-resource:3.4.0") // To insert into debug versions of app files
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

    // Android Dependencies
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.legacy:legacy-support-core-utils:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // Turns out AndroidStudio suggests the following preloaded Java versions, so need to grab the Kotlin versions!
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    // Could use androidx.activity but following gets it transitively giving us the needed viewModels() delegate
    implementation("androidx.fragment:fragment-ktx:1.5.3")

    // Google Dependencies
    implementation("com.google.android.material:material:1.6.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")

    // Kotlin Dependencies
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4") // Seems to transitively get coroutines-core & its Dispatchers.Main
}
repositories {
    mavenCentral()
}
