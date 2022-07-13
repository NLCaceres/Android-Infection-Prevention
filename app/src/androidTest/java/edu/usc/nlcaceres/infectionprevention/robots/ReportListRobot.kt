package edu.usc.nlcaceres.infectionprevention.robots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import edu.usc.nlcaceres.infectionprevention.R
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter.ReportViewHolder
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matcher
import edu.usc.nlcaceres.infectionprevention.helpers.util.childWithText
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItemLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.childWithPrefix
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeTo
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItem

class ReportListRobot: BaseRobot() {

  fun checkFiltersLoaded(vararg childTexts: String) {
    val childTextMatchers = childTexts.map { childWithText(it) }.toTypedArray() // Must be array, NOT List
    selectedFilterRV().check(matches(allOf(*childTextMatchers)))
  }
  fun checkListLoaded() {
    goToReport() // Scroll to a report with below matching text
    reportRV().check(matches(reportMatcher())) // Check if RV has children matching (therefore loaded)
  }

  companion object {
    fun selectedFilterRV(): ViewInteraction = onView(withId(R.id.selectedFilterRV)) // Filters to specify desired set of reports
    fun goToFilter(text: String) = reportRV().swipeToLabeled<ReportViewHolder>(text) // Make sure we're at correct item!
    fun closeFilter(text: String) = reportRV().tapItemLabeled<ReportViewHolder>(text) // Tap X button of filter to get rid of it

    fun reportRV(): ViewInteraction = onView(withId(R.id.reportRV)) // Actual list of reports
    // Need allof since A LOT of overlap between items
    fun reportMatcher(): Matcher<View> = allOf(childWithText("Hand Hygiene Violation"), // Using violationType + date
      childWithPrefix("May 19, 2019"), childWithText("Committed by Nicholas Caceres")) // + employeeName
    fun goToReport() = reportRV().swipeTo<ReportViewHolder>(reportMatcher()) // Make sure at correct item
    fun tapReport() = reportRV().tapItem<ReportViewHolder>(reportMatcher()) // Useful to go to a detail view one day

    fun filterButton(): ViewInteraction = onView(withId(R.id.sortFilterFloatingButton)) // Go to FilterView
  }
}