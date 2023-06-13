package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapBackButton
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import edu.usc.nlcaceres.infectionprevention.helpers.di.*
import org.junit.After
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import java.time.LocalDate

// @RunWith(AndroidJUnit4.class) // Not needed if set to default in build.gradle
@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class FragmentCreateReportTest: RoboTest() {
  @get:Rule(order = 0) // Best to start from MainActivity for a normal user Task experience
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createComposeRule()
  @get:Rule(order = 2)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @Before
  fun navigate_To_Create_Report_Activity() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity(composeTestRule) {
      checkViewLoaded()
      goCreateStandardReportLabeled("Hand Hygiene")
    }
    createReportActivity(composeTestRule) {
      checkCorrectTitle("New Hand Hygiene Observation")
      checkSpinnersLoaded()
    }
  }
  @After
  fun unregister_Idling_Resource() {
      IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  // Checking Initial Loading
  @Test fun check_Default_Header() { // Double Check!
    createReportActivity(composeTestRule) { checkCorrectTitle("New Hand Hygiene Observation") }
  }

  // Navigation
  @Test fun handle_Back_Navigation() { // May fail if not coming from mainActivity (which in this case is just closing app)
    tapBackButton()
    mainActivity(composeTestRule) { checkViewLoaded() }
  }
  @Test fun handle_Up_Navigation() {
    createReportActivity(composeTestRule) { pressUpButton() }
    mainActivity(composeTestRule) { checkViewLoaded() }
  }

  // Time and Date
  @Test fun select_Time() {
    createReportActivity(composeTestRule) {
      openTimeDialog()
      setTime(15, 24) // 3PM 24 minutes
      pressOkButton() // Goes to dateDialog
      pressCancelButton() // Cancel setting date half
      val localDate = LocalDate.now()
      val expectedDateTimeString = "3:24 PM ${localDate.monthValue}/${localDate.dayOfMonth}/${localDate.year}"
      checkTimeDateET(expectedDateTimeString) // BUT editText still updated, using today's date as a default!
    }
  }
  @Test fun select_Time_And_Date() {
    createReportActivity(composeTestRule) {
      openTimeDialog()
      setTime(15, 24) // 3PM 24 minutes
      pressOkButton() // Opens dateDialog
      setDate(2021, 4, 12) // This sets 2021, APRIL, 12th based on an 1-indexed month & day!
      // BUT the listener still receives a 0-indexed month, so it must compensate for it!
      pressOkButton() // Finalize date
      checkTimeDateET("3:24 PM 4/12/2021") // American date
    }
  }

  // Spinners
  @Test fun select_Employee() {
    createReportActivity(composeTestRule) {
      openEmployeeSpinner()
      selectEmployee("Melody Rios")
      checkSelectedEmployee("Melody Rios")
    }
  }
  @Test fun select_Health_Practice() {
    createReportActivity(composeTestRule) {
      openHealthPracticeSpinner()
      selectHealthPractice("Droplet")
      checkSelectedHealthPractice("Droplet")
      checkCorrectTitle("New Droplet Observation")
    }
  }
  @Test fun select_Location() {
    createReportActivity(composeTestRule) {
      openFacilitySpinner()
      selectFacility("USC 2 123")
      checkSelectedFacility("USC 2 123")
    }
  }

  // Finalizing New Report
  @Test fun submit_Without_Date() {
    // Since our Snackbar helper uses EspressoIdling to wait the 1500 to 2750ms its on screen,
    // Need to unregister the Idler if we want Espresso to be able to check if its on screen
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    createReportActivity(composeTestRule) {
      pressSubmitButton() // Should open dialog since no date selected
      checkAlertDialog()
      pressCancelButton()
      checkSnackBar() // Cancel button opens Snackbar telling user to set time/date
    }
  }
  @Test fun submit_With_Date() {
    createReportActivity(composeTestRule) {
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
    reportListFragment(composeTestRule) { // Should be no filters BUT should be a list of reports!
      checkInitListLoaded("Hand Hygiene", "John Smith", "May 18")
    }
  }
}