package edu.usc.nlcaceres.infectionprevention.robots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import edu.usc.nlcaceres.infectionprevention.R
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter.ReportViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter.SelectedFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.helpers.util.*
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matcher

class ReportListRobot: BaseRobot() {

  fun checkFiltersLoaded(vararg childTexts: String) {
    val childTextMatchers = childTexts.map { childWithText(it) }.toTypedArray() // Must be array, NOT List
    selectedFilterRV().check(matches(allOf(*childTextMatchers)))
  }
  fun removeSelectedFilterLabeled(text: String) {
    removeFilterButtonLabeled(text).tap()
  }
  fun checkInitListLoaded() {
    val violationType = "Hand Hygiene"
    val employeeName = "Nicholas Caceres"
    goToReport(violationType, employeeName) // Scroll to a report with below matching text
    // Check if RV has children matching (therefore loaded)
    reportRV().matching(reportMatcher(violationType, employeeName))
    checkListCount(5) // Should currently always start at 5
  }
  fun checkFirstSearchBarExpansion() { searchBar().isOnScreen(); searchBar().hasText(""); checkSearchBarFocused() }
  fun checkSearchBarExpanded() { searchBar().isOnScreen() }
  fun checkSearchBarFocused() { searchBar().isTheFocus() }
  fun checkSearchBarClosed() { searchBar().isNotInLayout() }
  fun checkListCount(viewCount: Int) { reportRV().matching(hasChildCount(viewCount)) }
  fun checkListOrder(vararg reports: Pair<String, String>) {
    reports.forEachIndexed { i, (violationType, employeeName) ->
      reportRV().matching(hasItemAtPosition(i, reportMatcher(violationType, employeeName)))
    }
  }

  fun startSelectingSortAndFilters() = filterButton().tap()
  fun goToSettings() = settingButton().tap()
  fun pressUpButton() = upButton().tap()
  fun expandSearchBar() = searchDialog().tap()
  fun closeSearchBar() = searchBarCloseButton().tap()
  fun tapAwayFromSearchBar() { reportRV().tap() } // Should close searchBar if editText empty
  fun enterSearchQuery(text: String) { searchBar().enterText(text) }
  fun enterQueryIntoFocusedItem(text: String) { searchBar().enterTextIntoFocus(text) }

  companion object {
    fun selectedFilterRvID(): Matcher<View> = withId(R.id.selectedFilterRV)
    fun selectedFilterRV(): ViewInteraction = onView(selectedFilterRvID()) // Filters to specify desired set of reports
    fun goToFilter(text: String) = selectedFilterRV().swipeToLabeled<SelectedFilterViewHolder>(text) // Make sure we're at correct item!
    fun removeFilterButtonLabeled(text: String): ViewInteraction = onView(allOf(isDescendantOfA(selectedFilterRvID()),
      hasSibling(withText(text)), withId(R.id.removeFilterButton))) // Need to tap X button itself! not the rest of the view.

    fun reportRV(): ViewInteraction = onView(withId(R.id.reportRV)) // Actual list of reports
    // Need allof since A LOT of overlap between items
    fun reportMatcher(violationType: String, employeeName: String): Matcher<View> = allOf(childWithText(
      "$violationType Violation"), childWithText("Committed by $employeeName"), childWithPrefix("May 19, 2019"))
    fun goToReport(violationType: String, employeeName: String) = reportRV().swipeTo<ReportViewHolder>(
      reportMatcher(violationType, employeeName)) // Make sure at correct item
    fun tapReport(violationType: String, employeeName: String) = reportRV().tapItem<ReportViewHolder>(
      reportMatcher(violationType, employeeName)) // Useful to go to a detailView soon

    fun filterButton(): ViewInteraction = onView(withId(R.id.sortFilterFloatingButton)) // Go to SortFilterView

    // Toolbar
    fun settingButton(): ViewInteraction = onView(withContentDescription("Settings"))
    fun upButton(): ViewInteraction = onView(withContentDescription("Navigate up"))
    fun searchDialog(): ViewInteraction = onView(withId(R.id.search_action)) // Original toolbar search button
    fun searchBar(): ViewInteraction = onView(withHint(R.string.search_hint)) // The editText "searchbar"
    fun searchBarCloseButton(): ViewInteraction = onView(withContentDescription("Collapse")) // ActionBar collapse button which closes searchBar!
  }
}