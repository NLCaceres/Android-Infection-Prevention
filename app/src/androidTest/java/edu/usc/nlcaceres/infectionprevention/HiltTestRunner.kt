package edu.usc.nlcaceres.infectionprevention

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/* Required to make instrumented tests run properly by allowing Hilt to inject as needed
 Must update defaultConfig.testInstrumentationRunner in module build.gradle file to run this class */
class HiltTestRunner: AndroidJUnitRunner() {
  override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
    return super.newApplication(cl, HiltTestApplication::class.java.name, context)
  }
}