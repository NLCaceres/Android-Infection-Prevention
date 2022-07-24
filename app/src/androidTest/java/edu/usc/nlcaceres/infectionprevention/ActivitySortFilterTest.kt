package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivitySortFilterTest: RoboTest() {
  @get:Rule // Best to start from MainActivity for a normal user Task experience
  val activityRule = ActivityScenarioRule(ActivityMain::class.java)

  @Before
  fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity {
      checkNavDrawerOpen(false) // Not open
      openNavDrawer()
      checkNavDrawerOpen(true) // Now Open
      goToReportList()
    }
    reportListActivity {
      checkListLoaded() // Verify made it to reportList (have to wait until RV loads)
      startSelectingSortAndFilters()
    }
    sortFilterActivity { checkLoaded() } // Filters loaded and ready to tap
  }
  @After
  fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  @Test fun selectFilter() {
    sortFilterActivity {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
    }
  }
  @Test fun selectFilterSingleSelection() { // RadioButton Single Selection Style (only 1 filter added)
    sortFilterActivity {
      openFilterGroupLabeled("Sort By")
      selectFilterLabeled("Date Reported")
      checkSelectedFilters("Date Reported") // Should be Date Reported
      selectFilterLabeled("Employee Name (A-Z)")
      checkSelectedFilters("Employee Name (A-Z)") // Should only be EmployeeName! Date Reported gone!
    }
  }
  @Test fun selectFilterMultiSelection() { // Checkbox multiple choice style (all checked added as filters)
    sortFilterActivity {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
    }
  }
  @Test fun removeSelectedFilter() { // Remove by X button, not by unchecking filters
    sortFilterActivity {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      removeSelectedFilterLabeled("Hand Hygiene")
      checkSelectedFilters()
      closeFilterGroupLabeled("Health Practice Type")
      // MUST always tidy up, close expanded filterGroups before choosing a different type
      // Or else following opening will fail due to ambiguous ref to filterRV
      openFilterGroupLabeled("Precaution Type")
      selectFilterLabeled("Standard")
      selectFilterLabeled("Isolation")
      checkSelectedFilters("Standard", "Isolation")
      removeSelectedFilterLabeled("Isolation")
      checkSelectedFilters("Standard")
      removeSelectedFilterLabeled("Standard")
      checkSelectedFilters()
    }
  }
  @Test fun resetFiltersChosen() {
    sortFilterActivity {
      // Select filters, reset and check no filters still there
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
      resetFilters()
      checkSelectedFilters()
      // Double check nothing is still checkmarked
      openFilterGroupLabeled("Health Practice Type")
      checkAllFiltersUnmarked() // All filters must be unchecked
    }
  }
  @Test fun finalizeFiltersChosen() { // Select filters, finalize choices, see them in reportList
    sortFilterActivity {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
      finalizeFilters()
    }
    reportListActivity { checkFiltersLoaded("Hand Hygiene", "PPE") }
  }
}