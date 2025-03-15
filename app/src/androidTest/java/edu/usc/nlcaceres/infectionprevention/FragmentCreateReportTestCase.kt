package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.helpers.util.withDataDescribedAs
import edu.usc.nlcaceres.infectionprevention.screens.CreateReportScreen
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import edu.usc.nlcaceres.infectionprevention.screens.ReportListScreen
import edu.usc.nlcaceres.infectionprevention.screens.SettingsScreen
import io.github.kakaocup.kakao.spinner.KSpinnerItem
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FragmentCreateReportTestCase: TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createAndroidComposeRule<ActivityMain>()

  @Before fun navigate_To_Create_Report_Activity() = run {
    step("Scroll to Create Hand Hygiene Report Button in MainActivityScreen, then tap it") {
      MainActivityScreen.tapHealthPracticeItem(composeTestRule, "Hand Hygiene")
      device.uiDevice.waitForIdle()
    }
    step("Check CreateReportScreen loaded") {
      composeTestRule.onNode(hasText("New Hand Hygiene Observation"))
    }
  }

  @Test fun check_Default_Header() = run {
    step("Check that 'Before' func navigated to CreateReportScreen via 'Create Hand Hygiene Report' Button") {
      composeTestRule.onNode(hasText("New Hand Hygiene Observation"))
    }
  }

  //! Navigation
  @Test fun handle_Back_Navigation() = run {
    step("Press back button from CreateReportScreen") { CreateReportScreen.pressBack() }
    step("Check MainActivityScreen reappeared") {
      device.uiDevice.waitForIdle()
      MainActivityScreen.precautionRV.firstChild<MainActivityScreen.PrecautionRvItem> { precautionTypeTV.containsText("Standard Report") }
    }
  }
  @Test fun handle_Up_Navigation() = run {
    step("Press Up button from CreateReportScreen") { CreateReportScreen.upButton.click() }
    step("Check MainActivityScreen reappeared") {
      device.uiDevice.waitForIdle()
      MainActivityScreen.precautionRV.lastChild<MainActivityScreen.PrecautionRvItem> { precautionTypeTV.containsText("Isolation Report") }
    }
  }
  @Test fun handle_Settings_Navigation() = run {
    step("Tap Settings Button in ActionBar to navigate from CreateReportScreen to FragmentSettings") {
      CreateReportScreen.settingsButton.click()
    }
    step("Check FragmentSettings loaded") {
      SettingsScreen.rvContainer.isDisplayed()
      SettingsScreen.personalInfoTitle { hasText("Personal Info") }
    }
  }

  //! Time and Date
  @Test fun select_Time() = run {
    step("Open TimePicker by tapping TimePickerEditText") {
      CreateReportScreen.timePickerET.click()
    }
    step("Set time to 3:34 PM") {
      device.uiDevice.waitForIdle()
      CreateReportScreen.timePicker.setTime(15, 34)
      CreateReportScreen.dialogOkButton.click() // Goes to DatePicker
    }
    step("Cancel DatePicker's AlertDialog") {
      CreateReportScreen.dialogCancelButton.click()
    }
    step("Check timePickerET's value") {
      val localDate = LocalDate.now()
      val expectedDateTimeString = "3:34 PM ${localDate.monthValue}/${localDate.dayOfMonth}/${localDate.year}"
      CreateReportScreen.timePickerET.hasText(expectedDateTimeString)
    }
  }
  @Test fun select_Time_And_Date() = run {
    step("Open TimePicker by tapping TimePickerEditText") {
      CreateReportScreen.timePickerET.click()
    }
    step("Set time to 3:24 PM") {
      device.uiDevice.waitForIdle()
      CreateReportScreen.timePicker.setTime(15, 24)
      CreateReportScreen.dialogOkButton.click()
    }
    step("Set date to April 12th 2021") {
      CreateReportScreen.datePicker.setDate(2021, 4, 12) // Set to 2021, APRIL, 12th based on an 1-indexed month & day!
      CreateReportScreen.dialogOkButton.click()
    }
    step("Check timePickerET's value") {
      CreateReportScreen.timePickerET.hasText("3:24 PM 4/12/2021") // American style
    }
  }

  //! Spinners
  @Test fun select_Employee() = run {
    step("Tap EmployeeSpinner to open its ItemView") {
      CreateReportScreen.employeeSpinner.open()
    }
    step("Select 'Melody Rios' from EmployeeSpinner") {
      CreateReportScreen.employeeSpinner.childWith<KSpinnerItem> { withMatcher(withDataDescribedAs<Employee>("Melody Rios")) }.click()
    }
    step("Check EmployeeSpinner updated its value to 'Melody Rios'") {
      CreateReportScreen.employeeSpinner.hasText("Melody Rios")
    }
  }
  @Test fun select_Health_Practice() = run {
    step("Tap HealthPracticeSpinner to open its ItemView") {
      CreateReportScreen.healthPracticeSpinner.open()
    }
    step("Select 'Droplet' from HealthPracticeSpinner") {
      CreateReportScreen.healthPracticeSpinner.childWith<KSpinnerItem> { withMatcher(withDataDescribedAs<HealthPractice>("Droplet")) }.click()
    }
    step("Check HealthPracticeSpinner updated its value to 'Droplet' and the HeaderTextView updated to 'New Droplet Observation'") {
      CreateReportScreen.healthPracticeSpinner.hasText("Droplet")
      CreateReportScreen.headerTV.hasText("New Droplet Observation")
    }
  }
  @Test fun select_Location() = run {
    step("Tap FacilitySpinner to open its ItemView") {
      CreateReportScreen.facilitySpinner.open()
    }
    step("Select 'USC 2 123' from FacilitySpinner") {
      CreateReportScreen.facilitySpinner.childWith<KSpinnerItem> { withMatcher(withDataDescribedAs<Location>("USC 2 123")) }.click()
    }
    step("Check FacilitySpinner updated its value to 'USC 2 123'") {
      CreateReportScreen.facilitySpinner.hasText("USC 2 123")
    }
  }

  //! Finalizing Report
  @Test fun submit_Without_Date() = run {
    step("Press submit button and check if alert dialog appears") {
      CreateReportScreen.submitButton.click()
      device.uiDevice.waitForIdle()
      CreateReportScreen.alertDialog.isCompletelyDisplayed()
    }
    step("Press cancel button in AlertDialog triggering Snackbar warning to set the time and date") {
      CreateReportScreen.dialogCancelButton.click()
      CreateReportScreen.snackbar.text.hasText("Tap the Time & Date text box above to set them up!")
      CreateReportScreen.alertDialog.doesNotExist()
      //? The following UIAutomator wait() Condition allows the UI to run BUT the tests to pause WITHOUT Thread.sleep(ms)
      device.uiDevice.wait(Until.gone(By.text("Tap the Time & Date text box above to set them up!")), 3100)
      CreateReportScreen.snackbar.doesNotExist() // Snackbar set to short duration, fading out after ~3secs
    }
  }
  @Test fun submit_With_Date() = run {
    step("Open the timePicker via timePickerET and set the time to 5:45 AM") {
      CreateReportScreen.timePickerET.click()
      CreateReportScreen.timePicker.setTime(5, 45) // 5:45 AM
      CreateReportScreen.dialogOkButton.click()
    }
    step("After datePicker opens due to timePicker's OK Button, set the date to Feb 24 2019") {
      CreateReportScreen.datePicker.setDate(2019, 2, 24)
      CreateReportScreen.dialogOkButton.click()
    }
    step("Check time and date is correct, then submit report") {
      CreateReportScreen.timePickerET.hasText("5:45 AM 2/24/2019")
      CreateReportScreen.submitButton.click()
    }
    step("Check if new Report added to ReportListScreen") {
      ReportListScreen.reportList.isDisplayed()
      assertEquals(5, ReportListScreen.reportList.getSize()) //TODO: Add new Report to ReportList (making this 6)
    }
  }
}