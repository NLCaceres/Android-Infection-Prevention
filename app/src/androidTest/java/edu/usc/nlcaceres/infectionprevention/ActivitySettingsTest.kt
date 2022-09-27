package edu.usc.nlcaceres.infectionprevention

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@HiltAndroidTest
class ActivitySettingsTest: RoboTest() {
  @get:Rule
  var rules = RuleChain.outerRule(HiltAndroidRule(this))
    .around(ActivityScenarioRule(ActivityMain::class.java))

  @Before
  fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    mainActivity { goToSettings() }
    settingsActivity { checkInitLoad() }
  }
  @After
  fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
  }

  @Test fun checkOneTimeDefinedPrefs() { // Check preferences made by preference.xml BUT defined on signup
    settingsActivity { // These do NOT launch dialogs
      findAndOpenPreference("Username")
      checkDialogNotLoaded("Username")

      findAndOpenPreference("Department")
      checkDialogNotLoaded("Department")

      findAndOpenPreference("Employee ID")
      checkDialogNotLoaded("Employee ID")
    }
  }
  @Test fun checkCommonlyChangedPrefs() { // Normal users should be able to see these. Defined in xml
    settingsActivity { // Launch EditTextDialogs, so should check for EditText hint too
      findAndOpenPreference("Phone Number")
      eraseOldPrefValue()
      checkDialogLoaded("Phone Number", "", "Ex: (123) 456-7890")
      dismissDialog() // Assuming dialog is open, cancelButton will work without test fail

      findAndOpenPreference("Password")
      eraseOldPrefValue()
      checkDialogLoaded("Password", "", "New password")
    }
  }
  @Test fun checkAdminPrefs() {
    settingsActivity {
      findAndOpenPreference("Healthcare Group or Clinic Name")
      checkDialogLoaded("Healthcare Group or Clinic Name", "", "New Name")
      dismissDialog()

      // Common hint for color-related preferences
      val colorHint = "Enter hex color i.e. #FFFFFF"

      findAndOpenPreference("Toolbar Color")
      checkDialogLoaded("Toolbar Color", "", colorHint)
      dismissDialog()

      findAndOpenPreference("Background Color")
      checkDialogLoaded("Background Color", "", colorHint)
      dismissDialog()

      findAndOpenPreference("Report Title Text Color")
      checkDialogLoaded("Report Title Text Color", "", colorHint)
    }
  }

  // Following should check summary providers differences when value is set vs not set
  // Also check that values are getting correctly set or formatted
  // Basic Process: 1. findAndOpen to enter new value 2. Check updated summary
  // 3. Open dialog again, enter empty value. 4. Check default summary
  @Test fun updateNormalPreferences() {
    settingsActivity {
      findAndOpenPreference("Phone Number") // Step 1
      enterNewPrefValue("123 456 7890")
      tapDialogOkButton()
      findPreference("Phone Number", "123 456 7890") // Step 2. Should be simple phone number
      openPreferenceEditDialog("Phone Number") // Step 3
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Phone Number", "Please enter your phone number") // Step 4

      findAndOpenPreference("Password")
      enterNewPrefValue("SomethingNew")
      tapDialogOkButton()
      findPreference("Password", "***ew") // Password hidden by summaryProvider so should have obscured text
      openPreferenceEditDialog("Password")
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Password", "Unable to find password")
    }
  }
  @Test fun updateAdminPreferences() {
    settingsActivity {
      findAndOpenPreference("Healthcare Group or Clinic Name")
      enterNewPrefValue("Some New Name")
      tapDialogOkButton()
      findPreference("Healthcare Group or Clinic Name", "Some New Name") // All admin values are simple when set
      openPreferenceEditDialog("Healthcare Group or Clinic Name")
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Healthcare Group or Clinic Name", "Hospital/Clinic Name missing")

      val summaryDefault = "Using default color"
      val someColor = "#123456"

      findAndOpenPreference("Toolbar Color")
      enterNewPrefValue(someColor)
      tapDialogOkButton()
      // TODO: Format hex colors so if user inputs "123456" (not "#123456"), summaryProvider adds "#"
      findPreference("Toolbar Color", someColor)
      openPreferenceEditDialog("Toolbar Color")
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Toolbar Color", summaryDefault)

      findAndOpenPreference("Background Color")
      enterNewPrefValue(someColor)
      tapDialogOkButton()
      findPreference("Background Color", someColor)
      openPreferenceEditDialog("Background Color")
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Background Color", summaryDefault)

      findAndOpenPreference("Report Title Text Color")
      enterNewPrefValue(someColor)
      tapDialogOkButton()
      findPreference("Report Title Text Color", someColor)
      openPreferenceEditDialog("Report Title Text Color")
      enterNewPrefValue("")
      tapDialogOkButton()
      findPreference("Report Title Text Color", summaryDefault)
    }
  }
}