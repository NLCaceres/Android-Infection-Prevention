package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.BindValue
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
class FragmentSortFilterTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @BindValue @JvmField
  var employeeRepository: EmployeeRepository = FakeEmployeeRepository()
  @BindValue @JvmField
  var healthPracticeRepository: HealthPracticeRepository = FakeHealthPracticeRepository()
  @BindValue @JvmField
  var locationRepository: LocationRepository = FakeLocationRepository()
  @BindValue @JvmField
  var precautionRepository: PrecautionRepository = FakePrecautionRepository()
  @BindValue @JvmField
  var reportRepository: ReportRepository = FakeReportRepository()

  @Before
  fun register_Idling_Resource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity {
      checkNavDrawerOpen(false) // Not open
      openNavDrawer()
      checkNavDrawerOpen(true) // Now Open
      goToReportList()
    }
    reportListFragment {  // Verify made it to reportList (have to wait until RV loads)
      checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
      startSelectingSortAndFilters()
    }
    sortFilterFragment { checkLoaded() } // Filters loaded and ready to tap
  }
  @After
  fun unregister_Idling_Resource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // Navigation
  @Test fun navigate_To_Settings() {
    sortFilterFragment { goToSettings() }
    settingsFragment { checkInitLoad() }
  }
  @Test fun navigate_Back_Up_To_Report_List() {
    sortFilterFragment { pressCloseButton() } // X button
    reportListFragment {
      checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
      checkFiltersLoaded()
    }
  }

  // Important Features (Select - Single/Multiselect and Removal)
  @Test fun select_Filter() {
    sortFilterFragment {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      checkMarkedFiltersIn(mapOf("Health Practice Type" to arrayListOf("Hand Hygiene"))) // Many different ways to make a map!
    }
  }
  @Test fun select_Filter_Single_Selection() { // RadioButton Single Selection Style (only 1 filter added)
    sortFilterFragment {
      openFilterGroupLabeled("Sort By")
      selectFilterLabeled("New Reports")
      checkSelectedFilters("New Reports") // Should be New Reports
      val selectedFilterMap = mutableMapOf("Sort By" to arrayListOf("New Reports"))
      checkMarkedFiltersIn(selectedFilterMap)

      selectFilterLabeled("Employee Name (A-Z)")
      checkSelectedFilters("Employee Name (A-Z)") // Should only be EmployeeName! Date Reported gone!
      selectedFilterMap["Sort By"]?.set(0, "Employee Name (A-Z)") // Set overwrites (vs add() pushing elems to right)
      checkMarkedFiltersIn(selectedFilterMap)
    }
  }
  @Test fun select_Filter_Multi_Selection() { // Checkbox multiple choice style (all checked added as filters)
    sortFilterFragment {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      val selectedFilterMap = buildMap { put("Health Practice Type", arrayListOf("Hand Hygiene")) }
      checkSelectedFilters("Hand Hygiene")
      checkMarkedFiltersIn(selectedFilterMap)

      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
      selectedFilterMap["Health Practice Type"]?.add("PPE")
      checkMarkedFiltersIn(selectedFilterMap)
    }
  }
  @Test fun remove_Selected_Filter() { // Remove by X button, not by unchecking filters
    sortFilterFragment {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      checkSelectedFilters("Hand Hygiene")
      val selectedFilterMap = mutableMapOf<String, ArrayList<String>>().apply {
        put("Health Practice Type", arrayListOf("Hand Hygiene"))
      }
      checkMarkedFiltersIn(selectedFilterMap)
      removeSelectedFilterLabeled("Hand Hygiene")
      checkSelectedFilters()
      selectedFilterMap["Health Practice Type"]?.removeAt(0)
      checkMarkedFiltersIn(selectedFilterMap) // Empty list so checking if filters all unchecked
      closeFilterGroupLabeled("Health Practice Type")
      selectedFilterMap.remove("Health Practice Type") // Delete key altogether
      // MUST always tidy up, close expanded filterGroups before choosing a different type
      // Or else following opening will fail due to ambiguous ref to filterRV
      openFilterGroupLabeled("Precaution Type")
      selectFilterLabeled("Standard")
      selectFilterLabeled("Isolation")
      checkSelectedFilters("Standard", "Isolation")
      selectedFilterMap["Precaution Type"] = arrayListOf("Standard", "Isolation")
      checkMarkedFiltersIn(selectedFilterMap)

      removeSelectedFilterLabeled("Isolation")
      selectedFilterMap["Precaution Type"]?.removeLast()
      checkSelectedFilters("Standard")
      checkMarkedFiltersIn(selectedFilterMap)

      removeSelectedFilterLabeled("Standard")
      selectedFilterMap["Precaution Type"]?.removeLast()
      checkSelectedFilters()
      checkMarkedFiltersIn(selectedFilterMap)
    }
  }

  // Toolbar Features
  @Test fun reset_Filters_Chosen() {
    sortFilterFragment {
      // Select filters, reset and check no filters still there
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
      val selectedFilterMap = mutableMapOf<String, ArrayList<String>>().apply {
        val healthPracticeArr = arrayListOf("Hand Hygiene", "PPE")
        put("Health Practice Type", healthPracticeArr)
      }
      checkMarkedFiltersIn(selectedFilterMap)
      resetFilters()
      checkSelectedFilters()
      selectedFilterMap.remove("Health Practice Type")
      checkMarkedFiltersIn(selectedFilterMap)
      // Double check nothing is still checkmarked
      openFilterGroupLabeled("Health Practice Type")
      checkAllFiltersUnmarked() // All filters must be unchecked
    }
  }
  @Test fun finalize_Filters_Chosen() { // Select filters, finalize choices, see them in reportList
    sortFilterFragment {
      openFilterGroupLabeled("Health Practice Type")
      selectFilterLabeled("Hand Hygiene")
      selectFilterLabeled("PPE")
      checkSelectedFilters("Hand Hygiene", "PPE")
      val selectedFilterMap = mutableMapOf<String, ArrayList<String>>().apply {
        val healthPracticeArr = arrayListOf("Hand Hygiene", "PPE")
        put("Health Practice Type", healthPracticeArr)
      }
      checkMarkedFiltersIn(selectedFilterMap)
      finalizeFilters()
    }
    reportListFragment { checkFiltersLoaded("Hand Hygiene", "PPE") }
  }
}