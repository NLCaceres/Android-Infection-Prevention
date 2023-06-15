package edu.usc.nlcaceres.infectionprevention.robots

import android.view.View
import androidx.compose.ui.test.*
import edu.usc.nlcaceres.infectionprevention.R
import androidx.test.espresso.Espresso.onView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.Matcher
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter.ExpandableFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter.SelectedFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.helpers.util.isHidden
import edu.usc.nlcaceres.infectionprevention.helpers.util.matching
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled

class SortFilterRobot: BaseRobot() {
  fun checkLoaded() {
    selectedFilterRV().isHidden()

    goToFilterGroup("Health Practice Type") // Make sure RV visible
    expandFilterGroup("Health Practice Type") // Open

    getComposeFilter("Contact Enteric")
    closeFilterGroup("Health Practice Type") // Close
  }
  private fun getComposeFilter(text: String) {
    composeTestRule.onNodeWithText(text)
  }
  fun openFilterGroupLabeled(text: String) {
    goToFilterGroup(text)
    expandFilterGroup(text)
  }
  fun checkMarkedFiltersIn(filterMap: Map<String, Pair<Boolean, ArrayList<String>>>) {
    for ((filterGroupName, tuple) in filterMap) {
      val (isSingleSelection, filterList) = tuple
      goToFilterGroup(filterGroupName)
      if (filterList.isEmpty()) { checkAllFilterRowsUnmarked(isSingleSelection) }
      else { // Jetpack Compose Testing + Accessibility works based on its Semantics systems,
        val matcher = if (isSingleSelection) ::isSelected else ::isOn // So RadioButtons are "Selected". CheckBoxes are "Toggled On/Off"
        filterList.forEach { filter ->
          getComposeFilter(filter)
          filterRowButton(filter).assert(matcher.invoke())
        }
      }
    }
  }
  private fun filterRowButton(filter: String) = composeTestRule.onNode(hasAnySibling(hasText(filter)))
  private fun checkAllFilterRowsUnmarked(isSingleSelection: Boolean) {
    val matcher = if (isSingleSelection) ::isNotSelected else ::isOff
    composeTestRule.onAllNodes(hasParent(hasTestTag("FilterRow"))).filter(hasClickAction()).assertAll(matcher.invoke())
  }
  fun closeFilterGroupLabeled(text: String) {
    goToFilterGroup(text)
    closeFilterGroup(text)
  }
  fun selectFilterLabeled(text: String) {
    getComposeFilter(text)
    toggleFilter(text)
  }
  private fun toggleFilter(text: String) { // Click checkbox/radio button
    composeTestRule.onNode(hasAnySibling(hasText(text))).performClick()
  }
  fun checkSelectedFilters(vararg filterNames: String) {
    if (filterNames.isEmpty()) { selectedFilterRV().matching(hasChildCount(0)); return }
    for (filterName in filterNames) {
      goToSelectedFilter(filterName) // If there's a failure then we know it must not exist which is wrong
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
  fun goToSettings() = settingButton().tap()
  fun pressCloseButton() = closeButton().tap() // X button on toolbar

  companion object {
    private fun selectedFilterRvID(): Matcher<View> = withId(R.id.selectedFiltersRecyclerView)
    fun selectedFilterRV(): ViewInteraction = onView(selectedFilterRvID()) // Filters to specify desired set of reports
    fun goToSelectedFilter(text: String) = selectedFilterRV().swipeToLabeled<SelectedFilterViewHolder>(text) // Make sure we're at correct item!
    fun removeFilterButtonLabeled(text: String): ViewInteraction = onView(allOf(isDescendantOfA(selectedFilterRvID()),
      hasSibling(withText(text)), withId(R.id.removeFilterButton))) // Need to tap X button itself! not the rest of the view.

    // Sort By, Precaution Type, Health Practice Type
    private fun expandableRvID(): Matcher<View> = withId(R.id.expandableFilterRecyclerView)
    private fun expandableRV(): ViewInteraction = onView(expandableRvID())
    fun goToFilterGroup(text: String) = expandableRV().swipeToLabeled<ExpandableFilterViewHolder>(text)
    // Since espresso can't tap multiple views at once. Need to avoid ambiguousRef AND always open THEN close filterGroups
    fun expandFilterGroup(text: String) = onView(allOf(isDescendantOfA(expandableRvID()), withText(text),
      hasSibling(allOf(withId(R.id.filterRecyclerView), not(isDisplayed()))))).tap() // Should only select those that need to be opened
    fun closeFilterGroup(text: String) = onView(allOf(isDescendantOfA(expandableRvID()), withText(text),
      hasSibling(allOf(withId(R.id.filterRecyclerView), isDisplayed())))).tap() // Should only select one that needs to be closed
    // Above is the tappable container, Below is the list of choices that appear on expansion
//    fun filterRvID(): Matcher<View> = withId(R.id.filterRecyclerView)
//    fun filterRvLabeled(text: String): ViewInteraction = onView(allOf(filterRvID(), hasSibling(withText(text))))
//    fun openFilterRV(): ViewInteraction = onView(allOf(filterRvID(), isDisplayed()))
//    fun goToFilterOption(text: String) = openFilterRV().swipeToLabeled<FilterViewHolder>(text)
//    fun selectFilter(text: String) = openFilterRV().tapItemLabeled<FilterViewHolder>(text)
    // Date Reported, Employee Name (A-Z) vs (Z-A). Standard vs Isolation. Hand Hygiene, PPE, Contact, Droplet, Airborne, Contact Enteric

    fun setFiltersButton(): ViewInteraction = onView(withId(R.id.set_filters_action))
    fun resetFiltersButton(): ViewInteraction = onView(withId(R.id.reset_filters_action))

    // Toolbar
    fun settingButton(): ViewInteraction = onView(withContentDescription("Settings"))
    fun closeButton(): ViewInteraction = onView(withContentDescription("Navigate up")) // Technically up button still!
  }
}