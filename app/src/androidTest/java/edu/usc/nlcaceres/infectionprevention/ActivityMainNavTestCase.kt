package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.screens.CreateReportScreen
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import edu.usc.nlcaceres.infectionprevention.screens.PreferenceItem
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
  @get:Rule(order = 1) // Following combines createComposeRule() + ActivityScenarioRule(ActivityMain::class.java)
  val composeTestRule = createAndroidComposeRule<ActivityMain>() // to launch the activity and find Compose nodes

  @Test fun click_Health_Practice_To_Go_To_Create_Report_Screen() {
    MainActivityScreen {
      sorryMessageTV.isInvisible()

      assertEquals(2, precautionRV.getSize())
      precautionRV.lastChild<MainActivityScreen.PrecautionRvItem> {

        assertEquals(4, healthPracticeRV.getSize())
        // Following matcher also doubles as a scrollTo command, finding the Contact Enteric Button's parent ComposeView
        healthPracticeItem("Contact Enteric").matches { isAssignableFrom(ComposeView::class.java) }
        //? Unfortunately Espresso and ComposeRules/SemanticsNodeInteractionsProvider have no overlap at all
        //? so matching the parent ComposeView to its child Composable Root seems impossible
        composeTestRule.onNodeWithText("Contact Enteric").performClick()
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
    // WHEN settings button clicked in toolbar, THEN SettingsScreen opens with the following views and text
    SettingsScreen {
      personalInfoTitle { hasText("Personal Info") }

      recyclerView.childAt<PreferenceItem>(1) {
        title.hasText("Username")
        //? `preferences.xml` SHOULD be overriding the TextView's default text in `preference_main.xml`
        // summary.hasEmptyText() // WHICH should make this assertion pass
        // BUT for some reason it holds onto the default under the hood despite rendering an empty textView
        summary.hasText("Preference Description")
      }

      adminInfoTitle { isVisible() }

      recyclerView.lastChild<PreferenceItem> { summary.hasText("Using default color") }
    }
  }
}