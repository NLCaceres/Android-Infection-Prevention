package edu.usc.nlcaceres.infectionprevention.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withText

class SettingsRobot: BaseRobot() {

  companion object {
    fun personalInfoHeader(): ViewInteraction = onView(withText("Personal Info"))
  }
}