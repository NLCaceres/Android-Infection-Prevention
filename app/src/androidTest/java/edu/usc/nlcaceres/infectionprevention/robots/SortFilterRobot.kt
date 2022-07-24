package edu.usc.nlcaceres.infectionprevention.robots

import android.view.View
import edu.usc.nlcaceres.infectionprevention.R
import androidx.test.espresso.Espresso.onView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matcher
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter.ExpandableFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.FilterAdapter.FilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter.SelectedFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.helpers.util.isHidden
import edu.usc.nlcaceres.infectionprevention.helpers.util.matching
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItemLabeled

class SortFilterRobot: BaseRobot() {
  fun checkLoaded() {
    selectedFilterRV().isHidden()
    goToFilterGroup("Health Practice Type") // Make sure RV visible
    expandFilterGroup("Health Practice Type") // Open
    goToFilterOption("Contact Enteric")
    closeFilterGroup("Health Practice Type") // Close
  }
  fun openFilterGroupLabeled(text: String) {
    goToFilterGroup(text)
    expandFilterGroup(text)
  }
  fun checkAllFiltersUnmarked() {
    filterRV().check(selectedDescendantsMatch(withId(R.id.filterCheckNameTextView), isNotChecked()))
  }
  fun closeFilterGroupLabeled(text: String) {
    goToFilterGroup(text)
    closeFilterGroup(text)
  }
  fun selectFilterLabeled(text: String) {
    goToFilterOption(text)
    selectFilter(text)
  }
  fun checkSelectedFilters(vararg filterNames: String) {
    if (filterNames.isEmpty()) { selectedFilterRV().matching(hasChildCount(0)); return }
    for (filterName in filterNames) {
      goToSelectedFilter(filterName)
      filterRV().matching(hasDescendant(allOf(withText(filterName), isChecked())))
    }
  }
  fun removeSelectedFilterLabeled(text: String) {
    removeFilterButtonLabeled(text).tap()
  }
  fun resetFilters() {
    resetFiltersButton().tap()
  }
  fun finalizeFilters() {
    setFiltersButton().tap()
  }

  companion object {
    fun selectedFilterRvID(): Matcher<View> = withId(R.id.selectedFiltersRecyclerView)
    fun selectedFilterRV(): ViewInteraction = onView(selectedFilterRvID()) // Filters to specify desired set of reports
    fun goToSelectedFilter(text: String) = selectedFilterRV().swipeToLabeled<SelectedFilterViewHolder>(text) // Make sure we're at correct item!
    fun removeFilterButtonLabeled(text: String): ViewInteraction = onView(allOf(isDescendantOfA(selectedFilterRvID()),
      hasSibling(withText(text)), withId(R.id.removeFilterButton))) // Need to tap X button itself! not the rest of the view.

    // Sort By, Precaution Type, Health Practice Type
    fun expandableRvID(): Matcher<View> = withId(R.id.expandableFilterRecyclerView)
    fun expandableRV(): ViewInteraction = onView(expandableRvID())
    fun goToFilterGroup(text: String) = expandableRV().swipeToLabeled<ExpandableFilterViewHolder>(text)
    fun expandFilterGroup(text: String) = expandableRV().tapItemLabeled<ExpandableFilterViewHolder>(text) // If open, it'll tap the middle (which doesn't close it!)
    fun closeFilterGroup(text: String) = onView(allOf(isDescendantOfA(expandableRvID()), withText(text))).tap() // So make sure to tap titleTextView to close
    // Above is the tappable container, Below is the list of choices that appear on expansion
    fun filterRV(): ViewInteraction = onView(allOf(withId(R.id.filterRecyclerView), isDisplayed())) //TODO: Include isDescendentOfA(parentTitle)?
    fun goToFilterOption(text: String) = filterRV().swipeToLabeled<FilterViewHolder>(text)
    fun selectFilter(text: String) = filterRV().tapItemLabeled<FilterViewHolder>(text)
    // Date Reported, Employee Name (A-Z) vs (Z-A). Standard vs Isolation. Hand Hygiene, PPE, Contact, Droplet

    fun setFiltersButton(): ViewInteraction = onView(withId(R.id.set_filters_action))
    fun resetFiltersButton(): ViewInteraction = onView(withId(R.id.reset_filters_action))
  }
}