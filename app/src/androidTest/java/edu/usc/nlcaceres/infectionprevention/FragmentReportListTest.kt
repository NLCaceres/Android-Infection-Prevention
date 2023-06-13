package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.di.*
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class FragmentReportListTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createComposeRule()
  @get:Rule(order = 2)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @Before
  fun register_Idling_Resource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity(composeTestRule) {
      checkNavDrawerOpen(false) // Not open
      openNavDrawer()
      checkNavDrawerOpen(true) // Now Open
      goToReportList()
    }
    reportListFragment(composeTestRule) { // Verify made it to reportList (have to wait until RV loads)
      checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
    }
  }
  @After
  fun unregister_Idling_Resource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // Navigation
  @Test fun navigate_To_Sort_Filter_Fragment() {
    reportListFragment(composeTestRule) { startSelectingSortAndFilters() }
    sortFilterFragment(composeTestRule) { checkLoaded() }
  }
  @Test fun navigate_To_Settings() {
    reportListFragment(composeTestRule) { goToSettings() }
    settingsFragment(composeTestRule) { checkInitLoad() }
  }
  @Test fun navigate_Back_To_Home_Page() {
    reportListFragment(composeTestRule) { pressUpButton() } // Back button on toolbar
    mainActivity(composeTestRule) { checkViewLoaded() }
  }

  // SearchBar filter
  @Test fun expand_SearchBar_ActionView_When_Empty() {
    reportListFragment(composeTestRule) {
      // Normal flow - Tap search icon, use searchBar, press x to close
      expandSearchBar()
      checkFirstSearchBarExpansion()
      closeSearchBar() // Use "X" (ActionBar collapse) button to close
      checkSearchBarClosed() // Actually checks if even in layout (since actionViews disappear on closure)

      expandSearchBar()
      checkFirstSearchBarExpansion() // EditText starts up again empty
      tapAwayFromSearchBar() // Tapping away USED to close it since editText is still empty
      checkSearchBarExpanded() // NOW searchBar losing focus doesn't close it, it stays open even if empty
    }
  }
  @Test fun expand_SearchBar_ActionView_With_Text() {
    reportListFragment(composeTestRule) {
      expandSearchBar()
      checkFirstSearchBarExpansion()
      enterSearchQuery("USC")
      checkListCount(2)
      tapAwayFromSearchBar() // EditText filled with "USC"
      checkSearchBarExpanded() // and it definitely stays open
      closeSearchBar() // True close by pressing "X" button
      checkSearchBarClosed()

      expandSearchBar()
      checkFirstSearchBarExpansion() // EditText empty despite previously being filled with "USC"
    }
  }
  @Test fun filter_With_SearchBar() {
    reportListFragment(composeTestRule) {
      expandSearchBar()
      checkFirstSearchBarExpansion()
      checkListCount(5)
      enterSearchQuery("USC")
      checkListCount(2) // Only 2 views left
      closeSearchBar() // True close by pressing "X" button
      checkSearchBarClosed()

      expandSearchBar()
      enterQueryIntoFocusedItem("USC") // Retry but this time being sure our editText gets autofocused
      checkListCount(2)
      closeSearchBar()

      expandSearchBar()
      enterSearchQuery("HSC")
      checkListCount(3)
    }
  }

  // ForResult Features (SortFilterFragment)
  @Test fun add_One_Filter() {
    reportListFragment(composeTestRule) { startSelectingSortAndFilters() }
    sortFilterFragment(composeTestRule) {
      checkLoaded()

      openFilterGroupLabeled("Precaution Type")
      selectFilterLabeled("Isolation")
      checkSelectedFilters("Isolation")
      finalizeFilters()
    }
    reportListFragment(composeTestRule) { // Works thanks to Hilt stubs
      checkFiltersLoaded("Isolation")
      checkListCount(2) // Backend still needs to send precautionType in production
    }
  }
  @Test fun add_Multiple_Filters() {
    reportListFragment(composeTestRule) { startSelectingSortAndFilters() }
    sortFilterFragment(composeTestRule) {
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
    reportListFragment(composeTestRule) {
      checkFiltersLoaded("Hand Hygiene", "Employee Name (A-Z)")
      checkListCount(2) // Should only get two hand hygiene related reports
      val handHygieneReports = arrayOf(Triple("Hand Hygiene", "John Smith", "May 18"),
        Triple("Hand Hygiene", "Melody Rios", "May 13"))
      checkListOrder(*handHygieneReports) // Should be in alpha order
    }
  }
  @Test fun remove_Filter_And_Reprocess_List() {
    reportListFragment(composeTestRule) { startSelectingSortAndFilters() }
    sortFilterFragment(composeTestRule) {
      checkLoaded()

      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      closeFilterGroupLabeled("Health Practice Type")

      finalizeFilters()
    }
    reportListFragment(composeTestRule) {
      checkFiltersLoaded("Hand Hygiene")
      checkListCount(2) // Only two reports that are hand hygiene based
      removeSelectedFilterLabeled("Hand Hygiene")
      checkFiltersLoaded()
      checkListCount(5) // Since no more filters left, back to our complete list
    }
  }
}