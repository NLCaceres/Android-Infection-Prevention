package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapBackButton
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import org.junit.After
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain

// @RunWith(AndroidJUnit4.class) // Not needed if set to default in build.gradle
@HiltAndroidTest
class ActivityCreateReportTest: RoboTest() {
  @get:Rule // Best to start from MainActivity for a normal user Task experience
  var rules = RuleChain.outerRule(HiltAndroidRule(this))
    .around(ActivityScenarioRule(ActivityMain::class.java))

  @Before
  fun navToCreateReportActivity() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity {
      checkViewLoaded()
      goCreateStandardReportLabeled("Hand Hygiene")
    }
    createReportActivity {
      checkCorrectTitle("New Hand Hygiene Observation")
      checkSpinnersLoaded()
    }
  }
  @After
  fun unregisterIdlingResource() {
      IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // Checking Initial Loading
  @Test fun checkDefaultHeader() { // Double Check!
    createReportActivity { checkCorrectTitle("New Hand Hygiene Observation") }
  }

  // Navigation
  @Test fun handleBackNavigation() { // May fail if not coming from mainActivity (which in this case is just closing app)
    tapBackButton()
    mainActivity { checkViewLoaded() }
  }
  @Test fun handleUpNavigation() {
    createReportActivity { pressUpButton() }
    mainActivity { checkViewLoaded() }
  }

  // Time and Date
  @Test fun selectTime() {
    createReportActivity {
      openTimeDialog()
      setTime(15, 24) // 3PM 24 minutes
      pressOkButton() // Goes to dateDialog
      pressCancelButton() // Cancel setting date half
      checkTimeDateET("3:24 PM") // BUT editText still updated!
    }
  }
  @Test fun selectTimeAndDate() {
    createReportActivity {
      openTimeDialog()
      setTime(15, 24) // 3PM 24 minutes
      pressOkButton() // Opens dateDialog
      setDate(2021, 4, 12) // Set it to 2021, MARCH (0 indexed month), 12 (1 index day)
      pressOkButton() // Finalize date
      checkTimeDateET("3:24 PM 4/12/2021") // American date
    }
  }

  // Spinners
  @Test fun selectEmployee() {
    createReportActivity {
      openEmployeeSpinner()
      selectEmployee("Nicholas Caceres")
      checkSelectedEmployee("Nicholas Caceres")
    }
  }
  @Test fun selectHealthPractice() {
    createReportActivity {
      openHealthPracticeSpinner()
      selectHealthPractice("Droplet")
      checkSelectedHealthPractice("Droplet")
      checkCorrectTitle("New Droplet Observation")
    }
  }
  @Test fun selectLocation() {
    createReportActivity {
      openFacilitySpinner()
      selectFacility("USC")
      checkSelectedFacility("USC 2 123")
    }
  }

  // Finalizing New Report
  @Test fun submitWithoutDate() {
    createReportActivity {
      pressSubmitButton() // Should open dialog since no date selected
      checkAlertDialog()
      pressCancelButton()
      checkSnackBar() // Cancel button opens snackbar telling user to set time/date
    }
  }
  @Test fun submitWithDate() {
    createReportActivity {
      openTimeDialog()
      setTime(5, 45) // 3PM 24 minutes
      pressOkButton() // Opens dateDialog
      setDate(2019, 2, 24) // Set it
      pressOkButton() // Finalize date
      checkTimeDateET("5:45 AM 2/24/2019") // American date
      pressSubmitButton()
    }
    // Technically goes through mainActivity to get to reportList BUT
    // Android seems to optimize around actually needing to nav to mainActivity
    // Instead, just running the resultHandler that launches the intent toward the reportList view
    reportListActivity {
      checkInitListLoaded() // Should be no filters BUT should be a list of reports!
    }
  }
}