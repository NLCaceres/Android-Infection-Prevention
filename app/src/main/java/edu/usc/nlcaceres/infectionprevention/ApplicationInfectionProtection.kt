package edu.usc.nlcaceres.infectionprevention

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/* Application Files are typically used for running processes when your app first is opened
* In this case, it's for Hilt which needs an entry point to inject our dependencies
* If needed to add functionality on start, we can override onCreate like any Activity would! */
@HiltAndroidApp
class InfectionProtectionApplication: Application() { }