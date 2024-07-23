package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.hilt.RepositoryModule
import edu.usc.nlcaceres.infectionprevention.screens.*
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityMainNavTestCase: TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) // Following combines createComposeRule() + ActivityScenarioRule(ActivityMain::class.java)
  val composeTestRule = createAndroidComposeRule<ActivityMain>() // to launch the activity and find Compose nodes

  @Test fun click_Health_Practice_To_Go_To_Create_Report_Screen() {
    MainActivityScreen {
      sorryMessageTV.isGone()

      assertEquals(2, precautionRV.getSize())
      precautionRV.lastChild<MainActivityScreen.PrecautionRvItem> {
        assertEquals(4, healthPracticeRV.getSize())
        // Following matcher also doubles as a scrollTo command, finding the Contact Enteric Button's parent ComposeView
        healthPracticeItem("Contact Enteric").matches { isAssignableFrom(ComposeView::class.java) }
        //? Unfortunately Espresso and ComposeRules/SemanticsNodeInteractionsProvider have no overlap at all
      } //? so linking/matching the parent ComposeView (above) to its child Composable Root (below) seems impossible
      tapHealthPracticeItem(composeTestRule, "Contact Enteric")
    }

    CreateReportScreen {
      headerTV.hasText("New Contact Enteric Observation")
    }
  }

  @Test fun click_NavDrawer_Generic_Report_Button_To_Go_To_Report_List_Fragment() {
    run {
      step("Open Navigation Drawer to navigate to FragmentReportList") {
        scenario(NavDrawerScenario { goToReportList() })
      }
      step("Check ReportList has loaded") {
        ReportListScreen {
          sorryMessageTV.isInvisible()
          assertEquals(5, reportList.getSize())
          reportList.firstChild<ReportListScreen.ReportRvItem> {
            reportTypeTitleTV.containsText("Hand Hygiene")
            employeeNameTV.containsText("John Smith")
            dateTV.containsText("May 18")
            locationTV.containsText("USC Unit: 4 Room: 202")
          }
        }
        onComposeScreen<ReportListComposeScreen>(composeTestRule) {
          // WHEN no filters rendered, THEN no container ComposeView displayed
          hasTestTag("SorterFilterListView")
          assertIsNotDisplayed()
          // Kaspresso's composeScreen preferably targets the child of the Root aka my ComposeView aka TestTag "SorterFilterListView"
          composeTestRule.onRoot().onChild().assert(hasTestTag("SorterFilterListView"))
          composeTestRule.onRoot().onChild().onChildren().assertCountEquals(0)
          sortFilterButtons.assertCountEquals(0) // Simplified version of the above
        }
      }
    }
  }

  @Test fun click_NavDrawer_Standard_Report_Only_Filter_To_Go_To_Report_List_Fragment() {
    run {
      step("Open Navigation Drawer to navigate to FragmentReportList with a Standard Report Filter") {
        scenario(NavDrawerScenario { goToStandardReportList() })
      }
      step("Check ReportList has loaded only the Standard Reports") {
        ReportListScreen {
          sorryMessageTV.isInvisible()
          assertEquals(3, reportList.getSize())
          reportList.lastChild<ReportListScreen.ReportRvItem>{
            reportTypeTitleTV.hasText("PPE Violation")
            employeeNameTV.hasText("Committed by Brian Ishida")
            dateTV.hasText("Apr 21, 2019 11:36PM")
            locationTV.hasText("Location: USC Unit: 2 Room: 123")
          }
        }
        onComposeScreen<ReportListComposeScreen>(composeTestRule) {
          // WHEN any filter is rendered, THEN its container composerView must be displayed with 1 Standard Filter child
          hasTestTag("SorterFilterListView")
          assertIsDisplayed()
          // Even when filters are rendered, the Root's child is STILL the ComposeView aka TestTag "SorterFilterListView"
          composeTestRule.onRoot().onChild().assert(hasTestTag("SorterFilterListView"))
          sortFilterButtons.assertCountEquals(1)
          sorterFilterButtons("Standard").assertExists()
        }
      }
    }
  }

  @Test fun click_NavDrawer_Isolation_Reports_Only_Filter_To_Go_To_Report_List_Fragment() {
    run {
      step("Open Navigation Drawer to navigate to FragmentReportList with an Isolation Report Filter") {
        scenario(NavDrawerScenario { goToIsolationReportList() })
      }
      step("Check ReportList has loaded only the Isolation Reports") {
        ReportListScreen {
          sorryMessageTV.isInvisible()
          assertEquals(2, reportList.getSize())
          reportList.childAt<ReportListScreen.ReportRvItem>(1) {
            reportTypeTitleTV.containsText("Droplet")
            employeeNameTV.containsText("Victor Richards")
            dateTV.containsText("May 25")
            locationTV.containsText("HSC Unit: 3 Room: 213")
          }
        }
        onComposeScreen<ReportListComposeScreen>(composeTestRule) {
          // WHEN any filter is rendered, THEN its container composerView must be displayed with 1 Isolation Filter child
          assertIsDisplayed()
          sortFilterButtons.assertCountEquals(1)
          sorterFilterButtons("Isolation").assertExists()
        }
      }
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