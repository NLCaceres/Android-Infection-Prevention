package edu.usc.nlcaceres.infectionprevention

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.IdlingRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import edu.usc.nlcaceres.infectionprevention.helpers.di.*
import org.junit.After
import org.junit.Before

// Best to remember: Turn off animations for instrumentedTests via devOptions in "About Emulated Device" (tap buildNum 10 times)
// Settings > devOptions > Drawing section > Turn off windowAnimationScale, transitionAnimationScale, animationDurationScale
// Also can reduce animation durations to 1 for debug build

/* Tests MainActivity navigation interactions around the rest of the app */
@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityMainNavTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) // This rule runs launch(ActivityClass) & can access the activity via this prop instead!
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
  }
  @After
  fun unregister_Idling_Resource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // androidTest dir DOESN'T allow `Back tick function names`() like test dir does! Snake case is fine for now!
  @Test fun click_Health_Practice_To_Go_To_Create_Report_Activity() {
      mainActivity {
        checkViewLoaded()
        goCreateIsoReportLabeled("Contact Enteric")
      }
      createReportActivity {
        checkCorrectTitle("New Contact Enteric Observation")
      }
  }

  @Test fun click_NavDrawer_Generic_Report_Button_To_Go_To_Report_List_Fragment() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToReportList()
      }
      reportListFragment { // Verify in reportList (have to wait until RV loads)
        checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
      }
  }

  // Next 2 tests fail w/out Hilt stubs since backend currently DOESN'T send precautionTypes w/ reports
  @Test fun click_NavDrawer_Standard_Report_Only_Filter_To_Go_To_Report_List_Fragment() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToFilteredStandardReportList()
      }
      reportListFragment {
        checkFiltersLoaded("Standard")
        checkListCount(3)
      }
  }
  @Test fun click_NavDrawer_Isolation_Reports_Only_Filter_To_Go_To_Report_List_Fragment() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToFilteredIsolationReportList()
      }
      reportListFragment {
        checkFiltersLoaded("Isolation")
        checkListCount(2)
      }
  }

  @Test fun click_Settings_Toolbar_Button_To_Go_To_Settings_Fragment() {
    mainActivity {
      checkNavDrawerOpen(false) // Not open
      goToSettings()
    }
    settingsFragment { checkInitLoad() }
  }
}