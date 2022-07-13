package edu.usc.nlcaceres.infectionprevention.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.helpers.util.hasText
import org.hamcrest.Matchers.endsWith

class CreateReportRobot: BaseRobot() {

  fun checkCorrectTitle(text: String) {
    titleTV().hasText(text) // Check title based on report item clicked in mainActivity
  }

  companion object {
    fun titleTV(): ViewInteraction = onView(withId(R.id.headerTV))
  }
}