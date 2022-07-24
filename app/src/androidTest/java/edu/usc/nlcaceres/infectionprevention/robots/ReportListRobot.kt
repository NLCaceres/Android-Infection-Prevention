package edu.usc.nlcaceres.infectionprevention.robots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import edu.usc.nlcaceres.infectionprevention.R
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter.ReportViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter.SelectedFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.helpers.util.childWithPrefix
import edu.usc.nlcaceres.infectionprevention.helpers.util.childWithText
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeTo
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItem
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItemLabeled
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matcher

class ReportListRobot: BaseRobot() {

  fun checkFiltersLoaded(vararg childTexts: String) {
    val childTextMatchers = childTexts.map { childWithText(it) }.toTypedArray() // Must be array, NOT List
    selectedFilterRV().check(matches(allOf(*childTextMatchers)))
  }
  fun checkListLoaded() {
    goToReport() // Scroll to a report with below matching text
    reportRV().check(matches(reportMatcher())) // Check if RV has children matching (therefore loaded)
  }
  fun startSelectingSortAndFilters() = filterButton().tap()

  companion object {
    fun selectedFilterRvID(): Matcher<View> = withId(R.id.selectedFilterRV)
    fun selectedFilterRV(): ViewInteraction = onView(selectedFilterRvID()) // Filters to specify desired set of reports
    fun goToFilter(text: String) = selectedFilterRV().swipeToLabeled<SelectedFilterViewHolder>(text) // Make sure we're at correct item!
    fun removeFilterButtonLabeled(text: String): ViewInteraction = onView(allOf(isDescendantOfA(selectedFilterRvID()),
      hasSibling(withText(text)), withId(R.id.removeFilterButton))) // Need to tap X button itself! not the rest of the view.

    fun reportRV(): ViewInteraction = onView(withId(R.id.reportRV)) // Actual list of reports
    // Need allof since A LOT of overlap between items
    fun reportMatcher(): Matcher<View> = allOf(childWithText("Hand Hygiene Violation"), // Using violationType + date
      childWithPrefix("May 19, 2019"), childWithText("Committed by Nicholas Caceres")) // + employeeName
    fun goToReport() = reportRV().swipeTo<ReportViewHolder>(reportMatcher()) // Make sure at correct item
    fun tapReport() = reportRV().tapItem<ReportViewHolder>(reportMatcher()) // Useful to go to a detailView soon

    fun filterButton(): ViewInteraction = onView(withId(R.id.sortFilterFloatingButton)) // Go to SortFilterView
  }
}