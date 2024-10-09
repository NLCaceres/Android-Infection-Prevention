// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        if (project.findProperty("composeCompilerReports") == "true") {
            compilerOptions.freeCompilerArgs.addAll(
                "-P",
                ("plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    "${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler")
            )
        }
        if (project.findProperty("composeCompilerMetrics") == "true") {
            compilerOptions.freeCompilerArgs.addAll(
                "-P",
                ("plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    "${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler")
            )
        }
    }
}
