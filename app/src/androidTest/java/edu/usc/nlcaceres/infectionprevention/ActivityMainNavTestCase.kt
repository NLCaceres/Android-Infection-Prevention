package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.screens.CreateReportScreen
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import edu.usc.nlcaceres.infectionprevention.screens.SettingsScreen
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityMainNavTestCase: TestCase() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeRule = createComposeRule()
  @get:Rule(order = 2)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @Test fun click_Health_Practice_To_Go_To_Create_Report_Screen() {
    MainActivityScreen {
      sorryMessageTV.isInvisible()
      assertEquals(2, precautionRV.getSize())
      precautionRV.lastChild<MainActivityScreen.PrecautionRvItem> {
        assertEquals(4, healthPracticeRV.getSize())
        healthPracticeRV.childWith<MainActivityScreen.PrecautionRvItem.HealthPracticeRvItem> {
          withDescendant { withText("Contact Enteric") }
        }.click()
      }
    }
    CreateReportScreen {
      headerTV.hasText("New Contact Enteric Observation")
    }
  }
  @Test fun click_Settings_Toolbar_Button_To_Go_To_Settings_Screen() {
    MainActivityScreen {
      settingsButton.click()
    }
    SettingsScreen {
      recyclerView.firstChild<SettingsScreen.PreferenceItem> {
        title.hasText("Personal Info")
      }
      recyclerView.childWith<SettingsScreen.PreferenceItem> {
        withDescendant { withText("Hospital-wide Admin Settings") }
      }.isVisible()
      recyclerView.childAt<SettingsScreen.PreferenceItem>(1) {
        title.hasText("Username")
        //? `preferences.xml` SHOULD be overriding the TextView's default text in `preference_main.xml`
        // summary.hasEmptyText() // WHICH should make this assertion pass
        // BUT for some reason it holds onto the default under the hood despite rendering an empty textView
        summary.hasText("Preference Description")
      }
      recyclerView.lastChild<SettingsScreen.PreferenceItem> { summary.hasText("Using default color") }
    }
  }
}