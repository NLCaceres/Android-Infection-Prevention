package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import edu.usc.nlcaceres.infectionprevention.screens.PreferenceItem
import edu.usc.nlcaceres.infectionprevention.screens.SettingsScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FragmentSettingsTestCase: TestCase() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createComposeRule()
  @get:Rule(order = 2)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @Before
  fun nav_to_SettingsScene() {
    MainActivityScreen { settingsButton.click() }
    SettingsScreen.personalInfoTitle { hasText("Personal Info") }
    SettingsScreen.adminInfoTitle { isVisible() }
  }

  @Test fun check_Back_Navigation() {
    // WHEN the back button is pressed
    SettingsScreen { pressBack() }
    // THEN the user will return to the last screen they were on
    MainActivityScreen.precautionRV.firstChild<MainActivityScreen.PrecautionRvItem> {
      precautionTypeTV.hasText("Create a New Standard Report")
    }
  }

  @Test fun check_One_Time_Defined_Preferences() {
    // WHEN some preferences are clicked, THEN they DON'T open a dialog and therefore are not updatable
    SettingsScreen {
      recyclerView.childAt<PreferenceItem>(1) {
        title.hasText("Username")
        click()
      }
      dialogView.doesNotExist()

      recyclerView.childAt<PreferenceItem>(2) {
        title.hasText("Department")
        click()
      }
      dialogView.doesNotExist()

      recyclerView.childAt<PreferenceItem>(3) {
        title.hasText("Employee ID")
        click()
      }
      dialogView.doesNotExist()
    }
  }

  // Normal users should be able to see these preferences, defined in `preferences.xml`
  @Test fun check_Commonly_Changed_Preferences() { // Check default values for initial loading of common prefs
    SettingsScreen {
      recyclerViewChild { withText("Phone Number") }.click()
      dialogView.isVisible()
      dialogTitle.hasText("Phone Number")
      dialogET.hasText("")
      dialogET.hasHint("Ex: (123) 456-7890")
      dialogCancelButton.click()
      dialogView.doesNotExist()

      recyclerViewChild { withText("Password") }.click()
      dialogView.isVisible()
      dialogTitle.hasText("Password")
      dialogET.hasText("")
      dialogET.hasHint("New password")
    }
  }
  @Test fun update_Common_Preferences() {
    SettingsScreen {
      val phoneNumPref = recyclerViewChild { withText("Phone Number") }
      phoneNumPref.click()
      // WHEN dialog opened via click and editText filled
      dialogET.typeText("123 456 7890")
      dialogOkButton.click()
      // THEN pref summary is updated to that value
      phoneNumPref.summary.hasText("123 456 7890")

      phoneNumPref.click()
      // WHEN dialog opened and editText emptied
      dialogET.replaceText("")
      dialogOkButton.click()
      // THEN pref summary provides suggestion to enter new phone number
      phoneNumPref.summary.hasText("Please enter your phone number")

      val passwordPref = recyclerViewChild { withText("Password") }
      passwordPref.click()
      // WHEN dialog initially opened via click and editText filled BUT not OK'd
      dialogET.typeText("Something new")
      dialogCancelButton.click()
      // THEN pref summary is updated to a default, in this case, a hint that no password was found
      passwordPref.summary.hasText("Unable to find password")

      passwordPref.click()
      // WHEN dialog opened and editText filled with value AND OK'd
      dialogET.replaceText("Something totally new")
      dialogOkButton.click()
      // THEN pref summary is updated to obscured password
      passwordPref.summary.hasText("***ew")

      passwordPref.click()
      // WHEN dialog reopened, editText checked, emptied, and OK'd
      dialogET.hasText("Something totally new") // TODO: Probably should also be obscured
      dialogET.replaceText("")
      dialogOkButton.click()
      // THEN dialogET has un-obscured password, and THEN pref summary has hint that no password was found
      passwordPref.summary.hasText("Unable to find password")
    }
  }

  @Test fun check_Admin_Preferences() { // Check default values for initial loading of admin prefs
    SettingsScreen {
      recyclerViewChild { withText("Healthcare Group or Clinic Name") }.click()
      dialogTitle.hasText("Healthcare Group or Clinic Name")
      dialogET.hasText("")
      dialogET.hasHint("New Name")
      dialogCancelButton.click()

      // Common hint for color-related preferences
      val colorHint = "Enter hex color i.e. #FFFFFF"

      recyclerViewChild { withText("Toolbar Color") }.click()
      dialogTitle.hasText("Toolbar Color")
      dialogET.hasText("")
      dialogET.hasHint(colorHint)
      dialogCancelButton.click()

      recyclerViewChild { withText("Background Color") }.click()
      dialogTitle.hasText("Background Color")
      dialogET.hasText("")
      dialogET.hasHint(colorHint)
      dialogCancelButton.click()

      recyclerViewChild { withText("Report Title Text Color") }.click()
      dialogTitle.hasText("Report Title Text Color")
      dialogET.hasText("")
      dialogET.hasHint(colorHint)
    }
  }
  @Test fun update_Admin_Preferences() {
    SettingsScreen {
      val healthcareGroupPref = recyclerViewChild { withText("Healthcare Group or Clinic Name") }
      healthcareGroupPref.click()
      // WHEN dialog opened and editText filled
      dialogET.typeText("Some New Name")
      dialogOkButton.click()
      // THEN pref summary updated to that value
      healthcareGroupPref.summary.hasText("Some New Name")

      healthcareGroupPref.click()
      // WHEN dialog re-opened and editText emptied
      dialogET.replaceText("")
      dialogOkButton.click()
      // THEN pref summary is changed to a hint that the Hospital/Clinic name is missing
      healthcareGroupPref.summary.hasText("Hospital/Clinic Name missing")

      val summaryDefault = "Using default color"
      val someColor = "#123456"
      // TODO: Format hex colors so if user inputs "123456" (not "#123456"), summaryProvider adds "#"
      val toolbarColorPref = recyclerViewChild { withText("Toolbar Color") }
      toolbarColorPref.click()
      // WHEN dialog opened and editText filled
      dialogET.typeText(someColor)
      dialogOkButton.click()
      // THEN summary receives that value
      toolbarColorPref.summary.hasText(someColor)

      toolbarColorPref.click()
      // WHEN dialog re-opened and editText emptied BUT not OK'd
      dialogET.replaceText("")
      dialogCancelButton.click()
      // THEN the color pref remains the same
      toolbarColorPref.summary.hasText(someColor)

      toolbarColorPref.click()
      // WHEN re-opened again, THEN the dialog editText text value remains the same
      dialogET.hasText(someColor)
      dialogET.replaceText("") // WHEN the editText is emptied and OK'd
      dialogOkButton.click()
      toolbarColorPref.summary.hasText(summaryDefault) // THEN the summary hints that default colors will be used

      // WHEN other color preferences are similarly updated
      val backgroundColorPref = recyclerViewChild { withText("Background Color") }
      backgroundColorPref.click()
      dialogET.hasText("")
      dialogET.typeText(someColor)
      dialogOkButton.click()
      backgroundColorPref.summary.hasText(someColor) // THEN the value is filled in the summary
      backgroundColorPref.click()
      // WHEN other color preferences are similarly emptied
      dialogET.replaceText("")
      dialogOkButton.click()
      // THEN the summary also hints that default color will be used across screens' views
      backgroundColorPref.summary.hasText(summaryDefault)

      // WHEN other color preferences are similarly updated
      val reportTitleTextColorPref = recyclerViewChild { withText("Report Title Text Color") }
      reportTitleTextColorPref.click()
      dialogET.typeText(someColor)
      dialogOkButton.click()
      reportTitleTextColorPref.summary.hasText(someColor) // THEN the value is filled in the summary
      reportTitleTextColorPref.click()
      // WHEN other color preferences are similarly emptied
      dialogET.replaceText("")
      dialogOkButton.click()
      // THEN the summary also hints that default color will be used across screens' views
      reportTitleTextColorPref.summary.hasText(summaryDefault)
    }
  }
}