package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ActivityReportListTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

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
      checkInitListLoaded() // Verify made it to reportList (have to wait until RV loads)
    }
  }
  @After
  fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // Navigation
  @Test fun navigateToSortFilterActivity() {
    reportListActivity { startSelectingSortAndFilters() }
    sortFilterActivity { checkLoaded() }
  }
  @Test fun navigateToSettings() {
    reportListActivity { goToSettings() }
    settingsActivity { checkInitLoad() }
  }
  @Test fun navigateBackToReportList() {
    reportListActivity { pressUpButton() } // Back button on toolbar
    mainActivity { checkViewLoaded() }
  }

  // SearchBar filter
  @Test fun expandSearchActionBarViewWhenEmpty() {
    reportListActivity {
      // Normal flow - Tap search icon, use searchBar, press x to close
      expandSearchBar()
      checkFirstSearchBarExpansion()
      closeSearchBar() // Use "X" (ActionBar collapse) button to close
      checkSearchBarClosed() // Actually checks if even in layout (since actionViews disappear on closure)

      expandSearchBar()
      checkFirstSearchBarExpansion() // EditText starts up again empty
      tapAwayFromSearchBar() // Tapping away will close it since editText is still empty
      checkSearchBarClosed()
    }
  }
  @Test fun expandSearchActionBarViewWithText() {
    reportListActivity {
      expandSearchBar()
      checkFirstSearchBarExpansion()
      enterSearchQuery("USC")
      checkListCount(1)
      tapAwayFromSearchBar() // Since editText filled with "USC"
      checkSearchBarExpanded() // editText will stay open
      closeSearchBar() // True close by pressing "X" button
      checkSearchBarClosed()

      expandSearchBar()
      checkFirstSearchBarExpansion() // EditText empty despite previously being filled with "USC"
    }
  }
  @Test fun filterWithSearchbar() {
    reportListActivity {
      expandSearchBar()
      checkFirstSearchBarExpansion()
      checkListCount(5)
      enterSearchQuery("USC")
      checkListCount(1) // Should only have one view left
      closeSearchBar() // True close by pressing "X" button
      checkSearchBarClosed()

      expandSearchBar()
      enterQueryIntoFocusedItem("USC") // Retry but this time being sure our editText gets autofocused
      checkListCount(1)
      closeSearchBar()

      expandSearchBar()
      enterSearchQuery("HSC")
      checkListCount(4)
    }
  }

  // ForResult Features (SortFilterActivity)
  @Test fun addOneFilter() {
    reportListActivity { startSelectingSortAndFilters() }
    sortFilterActivity {
      checkLoaded()

      openFilterGroupLabeled("Precaution Type")
      selectFilterLabeled("Isolation")
      checkSelectedFilters("Isolation")
      finalizeFilters()
    }
    reportListActivity {
      checkFiltersLoaded("Isolation")
      checkListCount(0) // TODO: Currently broken. Backend needs to send precautionName with healthPractice in Report
    }
  }
  @Test fun addMultipleFilters() {
    reportListActivity { startSelectingSortAndFilters() }
    sortFilterActivity {
      checkLoaded()

      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      closeFilterGroupLabeled("Health Practice Type")

      openFilterGroupLabeled("Sort By")
      selectFilterLabeled("Employee Name (A-Z)")
      checkSelectedFilters("Hand Hygiene", "Employee Name (A-Z)")
      finalizeFilters()
    }
    reportListActivity {
      checkFiltersLoaded("Hand Hygiene", "Employee Name (A-Z)")
      checkListCount(2) // Should only get two hand hygiene related reports
      checkListOrder("Hand Hygiene" to "Edwin Toribio", "Hand Hygiene" to "Nicholas Caceres") // Should be in alpha order
    }
  }
  @Test fun removeFilterAndReprocessList() {
    reportListActivity { startSelectingSortAndFilters() }
    sortFilterActivity {
      checkLoaded()

      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      closeFilterGroupLabeled("Health Practice Type")

      finalizeFilters()
    }
    reportListActivity {
      checkFiltersLoaded("Hand Hygiene")
      checkListCount(2) // Only two reports that are hand hygiene based
      removeSelectedFilterLabeled("Hand Hygiene")
      checkFiltersLoaded()
      checkListCount(5) // Since no more filters left, back to our complete list
    }
  }
}