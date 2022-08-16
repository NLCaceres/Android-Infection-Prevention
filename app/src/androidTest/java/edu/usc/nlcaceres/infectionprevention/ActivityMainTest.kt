package edu.usc.nlcaceres.infectionprevention

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.IdlingRegistry
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

// Best to remember: Turn off animations for instrumentedTests via devOptions in "About Emulated Device" (tap buildNum 10 times)
// Settings > devOptions > Drawing section > Turn off windowAnimationScale, transitionAnimationScale, animationDurationScale
// Also can reduce animation durations to 1 for debug build

/* Tests MainActivity and its interactions toward the rest of the app */
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
        checkInitListLoaded() // Verify in reportList (have to wait until RV loads)
      }
  }

  // TODO: Following two tests fail since Precaution Type Filter currently filters out all reports
  @Test fun clickNavDrawerStandardReportFilterToLaunchReportListActivity() {
      mainActivity {
        checkNavDrawerOpen(false) // Not open
        openNavDrawer()
        checkNavDrawerOpen(true) // Now Open
        goToFilteredStandardReportList()
      }
      reportListActivity {
        checkFiltersLoaded("Standard")
        checkInitListLoaded() // Verify in reportList (have to wait until RV loads)
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
        checkInitListLoaded() // Verify in reportList (have to wait until RV loads)
      }
  }

  @Test fun clickSettingsToolbarButtonToLaunchSettingsActivity() {
    mainActivity {
      checkNavDrawerOpen(false) // Not open
      goToSettings()
    }
    settingsActivity { checkInitLoad() }
  }
}