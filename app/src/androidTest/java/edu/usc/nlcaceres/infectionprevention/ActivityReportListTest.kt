package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.ReportRepository
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakePrecautionRepository
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakeReportRepository
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityReportListTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @BindValue @JvmField // Each test gets its own version of the repo so no variable pollution like the closures
  var precautionRepository: PrecautionRepository = FakePrecautionRepository().apply { populateList() }
  @BindValue @JvmField
  var reportRepository: ReportRepository = FakeReportRepository().apply { populateList() }

  @Before
  fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity {
      checkNavDrawerOpen(false) // Not open
      openNavDrawer()
      checkNavDrawerOpen(true) // Now Open
      goToReportList()
    }
    reportListActivity { // Verify made it to reportList (have to wait until RV loads)
      checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
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
  @Test fun navigateBackToHomePage() {
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
      checkListCount(2)
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
    reportListActivity { // Works thanks to Hilt stubs
      checkFiltersLoaded("Isolation")
      checkListCount(2) // Backend still needs to send precautionType in production
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
      val handHygieneReports = arrayOf(Triple("Hand Hygiene", "John Smith", "May 18"),
        Triple("Hand Hygiene", "Melody Rios", "May 13"))
      checkListOrder(*handHygieneReports) // Should be in alpha order
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