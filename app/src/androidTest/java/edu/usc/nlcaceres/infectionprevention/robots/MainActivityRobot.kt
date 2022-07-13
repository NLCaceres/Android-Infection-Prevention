package edu.usc.nlcaceres.infectionprevention.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.Matchers.allOf
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter.PrecautionViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.HealthPracticeAdapter.PracticeViewHolder
import org.hamcrest.Matchers.endsWith
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import edu.usc.nlcaceres.infectionprevention.helpers.util.isOnScreen
import edu.usc.nlcaceres.infectionprevention.helpers.util.hasChildWithText
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeTo
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItemLabeled

class MainActivityRobot : BaseRobot() {

  fun checkViewLoaded() { // Be sure RVs are visible && data loaded in (to interact with viewHolders)
    secondHorizontalRV().isOnScreen() // Quick check secondRV is visible
    firstHorizontalRV().hasChildWithText("Hand Hygiene") // Quick check data loaded
  }
  fun goCreateStandardReportLabeled(text: String) {
    goToStandardItem(text) // Make sure its visible!
    tapStandardItem(text) // then tap, triggering change to ActivityCreateReport
  }
  fun goCreateIsoReportLabeled(text: String) {
    goToIsolationItem(text) // Make sure its visible
    tapIsolationItem(text) // then tap, triggering change to ActivityCreateReport
  }

  // Toolbar Actions (menu button, settings button available)
  fun checkNavDrawerOpen(open: Boolean = true) {
    val drawerOpen = if (open) isOpen() else isClosed() // Could include Gravity.START as param to both but used by default!
    navDrawer().check(matches(drawerOpen)) // Open OR Closed drawer?
  }
  fun openNavDrawer(open: Boolean = true) {
    val willOpen = if (open) DrawerActions.open() else DrawerActions.close() // Could also include Gravity.START but still a default!
    navDrawer().perform(willOpen)
  }
  fun goToReportList() {
    checkNavDrawerOpen(true) // Require nav drawer to be open before trying to tap button to navigate away
    navNormalReportListButton().tap() // Go to ReportListView
  }
  fun goToFilteredStandardReportList() {
    checkNavDrawerOpen(true)
    navFilterStandardReportListButton().tap() // Go to ReportListView with only standard reports left after filter
  }
  fun goToFilteredIsolationReportList() {
    checkNavDrawerOpen(true)
    navFilterIsoReportListButton().tap() // Go to ReportListView with only isolation reports left after filter
  }
  fun goToSettings() {
    settingButton().tap()
  }

  companion object {
    fun precautionRV(): ViewInteraction = onView(withId(R.id.precautionRV)) // Outer
    fun firstHorizontalRV(): ViewInteraction = onView(allOf(hasSibling(withText(endsWith("Standard Report"))),
      withId(R.id.horizontalRecycleView))) // Nested RV (An item of outer RV)
    fun goToStandardItem(text: String) = firstHorizontalRV().swipeToLabeled<PracticeViewHolder>(text)
    fun tapStandardItem(text: String) = firstHorizontalRV().tapItemLabeled<PracticeViewHolder>(text)
    fun secondHorizontalRV(): ViewInteraction {
      precautionRV().swipeTo<PrecautionViewHolder>(1) // Be sure its visible
      return onView(allOf(hasSibling(withText(endsWith("Isolation Report"))), withId(R.id.horizontalRecycleView)))
    }
    fun goToIsolationItem(text: String) = secondHorizontalRV().swipeToLabeled<PracticeViewHolder>(text)
    fun tapIsolationItem(text: String) = secondHorizontalRV().tapItemLabeled<PracticeViewHolder>(text)

    // Toolbar views
    fun navDrawer(): ViewInteraction = onView(withId(R.id.myNavDrawer))
    fun navNormalReportListButton(): ViewInteraction = onView(withId(R.id.nav_reports))
    fun navFilterStandardReportListButton(): ViewInteraction = onView(withId(R.id.nav_standard_precautions))
    fun navFilterIsoReportListButton(): ViewInteraction = onView(withId(R.id.nav_isolation_precautions))
    fun settingButton(): ViewInteraction = onView(withId(R.id.action_settings)) // Uses system id to find
  }
}