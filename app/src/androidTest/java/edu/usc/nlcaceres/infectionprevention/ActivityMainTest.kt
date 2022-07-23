package edu.usc.nlcaceres.infectionprevention

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildPrecaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType
import edu.usc.nlcaceres.infectionprevention.data.ReportService
import edu.usc.nlcaceres.infectionprevention.data.ReportService.PrecautionApiInterface
import retrofit2.Response
import androidx.test.espresso.IdlingRegistry
import edu.usc.nlcaceres.infectionprevention.helpers.util.RepeatRule
import edu.usc.nlcaceres.infectionprevention.helpers.util.RepeatTest
import edu.usc.nlcaceres.infectionprevention.robots.*
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.helpers.util.isOnScreen
import edu.usc.nlcaceres.infectionprevention.adapters.HealthPracticeAdapter.PracticeViewHolder
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import retrofit2.Call
import retrofit2.Retrofit

/* Tests MainActivity and its interactions toward the rest of the app */
// Probably best to always remember to turn off animations (especially in this activity where flashing icons are present)
// Go to settings > devOptions > Drawing section > Turn off windowAnimationScale, transitionAnimationScale, animationDurationScale
// May require turning on devOptions in "About Emulated Device" (tap buildNum 10 times)
@RunWith(MockitoJUnitRunner::class)
class ActivityMainTest: RoboTest() {
  @get:Rule // Runs launch(ActivityClass) that can be accessed via this prop instead!
  val activityRule = ActivityScenarioRule(ActivityMain::class.java)

  @Before
  fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
  }
  @After
  fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  @Test fun clickHealthPracticeToLaunchCreateActivity() {
      mainActivity {
        checkViewLoaded()
        goCreateIsoReportLabeled("Contact Enteric")
      }
      createReportActivity {
        checkCorrectTitle("New Contact Enteric Observation")
      }
  }

  @Test fun clickNavDrawerReportButtonToLaunchReportListActivity() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToReportList()
      }
      reportListActivity {
        checkListLoaded() // Verify in reportList (have to wait until RV loads)
      }
  }

  @Test fun clickNavDrawerStandardReportFilterToLaunchReportListActivity() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToFilteredStandardReportList()
      }
      reportListActivity {
        checkFiltersLoaded("Standard")
        checkListLoaded() // Verify in reportList (have to wait until RV loads)
      }
  }
  @Test fun clickNavDrawerIsoReportFilterToLaunchReportListActivity() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToFilteredIsolationReportList()
      }
      reportListActivity {
        checkFiltersLoaded("Isolation")
        checkListLoaded() // Verify in reportList (have to wait until RV loads)
      }
  }

  @Test fun clickSettingsToolbarButtonToLaunchSettingsActivity() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        goToSettings()
        SettingsRobot.personalInfoHeader().isOnScreen()
      }
  }
}