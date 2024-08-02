package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.screens.NavDrawerScenario
import edu.usc.nlcaceres.infectionprevention.screens.ReportListComposeScreen
import edu.usc.nlcaceres.infectionprevention.screens.ReportListScreen
import edu.usc.nlcaceres.infectionprevention.screens.SettingsScreen
import edu.usc.nlcaceres.infectionprevention.screens.SortFilterComposeScreen
import edu.usc.nlcaceres.infectionprevention.screens.SortFilterScreen
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FragmentSortFilterTestCase: TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createAndroidComposeRule<ActivityMain>()

  @Before fun navigateToSortFilterScreen() = run {
    step("Navigate to ReportListScreen") { scenario(NavDrawerScenario { goToReportList() }) }
    step("Check ReportList loaded and navigate to SortFilterScreen") {
      device.uiDevice.waitForIdle() // Wait for NavDrawer to close and ReportListScreen to animate in
      ReportListScreen.reportList.isDisplayed()
      ReportListScreen.sortFilterButton.click()
    }
    step("Check SortFilterScreen loaded") {
      device.uiDevice.waitForIdle() // Wait for SortFilterScreen to load in
      SortFilterScreen.expandableFilterList.isVisible()
      SortFilterScreen.expandableFilterList("Sort By").isDisplayed()
    }
  }

  //! Navigation
  @Test fun navigate_To_Settings() = run {
    step("Tap SettingsButton to navigate to SettingsScreen") { SortFilterScreen.settingsButton.click() }
    step("Check SettingsScreen loaded") {
      SettingsScreen.rvContainer.isDisplayed()
      SettingsScreen.personalInfoTitle { hasText("Personal Info") }
    }
  }
  @Test fun navigate_Back_Up_To_Report_List() = run {
    step("Press back button from SortFilterScreen") { SortFilterScreen.pressBack() }
    step("Check ReportListScreen visible") {
      ReportListScreen { reportList.isDisplayed(); assertEquals(5, reportList.getSize()) }
    }
  }

  //! Selection Features - Radio Button & Checkboxes
  @Test fun select_Filter() = run {
    step("Select Sort By Older Reports") {
      SortFilterScreen {
        openExpandableFilterList("Sort By")
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Radio Button was toggled and 1 selected filter listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Older Reports")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(1)
        selectedFilterButton("Older Reports").assertIsDisplayed()
      }
    }
    step("Select Droplet Health Practice Type Filter") {
      SortFilterScreen {
        openExpandableFilterList("Health Practice Type")
        tapFilterItem(composeTestRule, "Droplet")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Checkbox was toggled and BOTH selected filters listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Droplet")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(2)
        selectedFilterButton("Droplet").assertIsDisplayed()
        selectedFilterButton("Older Reports").assertIsDisplayed()
      }
    }
  }
  @Test fun select_Filter_Single_Selection() = run {
    step("Select Sort By Older Reports") {
      SortFilterScreen {
        openExpandableFilterList("Sort By")
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Radio Button was toggled and 1 selected filter listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Older Reports")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(1)
        selectedFilterButton("Older Reports").assertIsDisplayed()
      }
    }
    step("Select Sort By New Reports") {
      SortFilterScreen {
        tapFilterItem(composeTestRule, "New Reports")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Radio Button was toggled and STILL 1 selected filter listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "New Reports")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(1)
        selectedFilterButton("New Reports").assertIsDisplayed()
      }
    }
    //* Checkboxes have no impact on Radio Buttons
    step("Select Standard Precaution Type Filter") {
      SortFilterScreen {
        openExpandableFilterList("Precaution Type")
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Checkbox toggled, has no effect on Radio Buttons, and now 2 selected filters listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "New Reports")
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Standard")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(2)
        selectedFilterButton("New Reports").assertIsDisplayed()
        selectedFilterButton("Standard").assertIsDisplayed()
      }
    }
  }
  @Test fun select_Filter_Multi_Selection() = run {
    step("Select Standard AND Isolation Precaution Type Filters") {
      SortFilterScreen {
        openExpandableFilterList("Precaution Type")
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle()
        tapFilterItem(composeTestRule, "Isolation")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check both Checkboxes toggled and 2 selected filters listed at top of screen") {
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Standard")
      SortFilterScreen.findSelectedFilterItem(composeTestRule, "Isolation")
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(2)
        selectedFilterButton("Standard").assertIsDisplayed()
        selectedFilterButton("Isolation").assertIsDisplayed()
      }
    }
    step("Select Contact AND Contact Enteric Health Practice Types Filters") {
      SortFilterScreen {
        openExpandableFilterList("Health Practice Type")
        tapFilterItem(composeTestRule, "Contact")
        device.uiDevice.waitForIdle()
        tapFilterItem(composeTestRule, "Contact Enteric")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check ALL tapped Checkboxes toggled and 4 selected filters listed at top of screen") {
      SortFilterScreen {
        findSelectedFilterItem(composeTestRule, "Standard")
        findSelectedFilterItem(composeTestRule, "Isolation")
        findSelectedFilterItem(composeTestRule, "Contact")
        findSelectedFilterItem(composeTestRule, "Contact Enteric")
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(4)
        selectedFilterButton("Standard").assertIsDisplayed()
        selectedFilterButton("Isolation").assertIsDisplayed()
        selectedFilterButton("Contact").assertIsDisplayed()
        selectedFilterButton("Contact Enteric").assertIsDisplayed()
      }
    }
    //* Radio buttons have no impact on checkboxes
    step("Select Sort By Older Reports") {
      SortFilterScreen {
        openExpandableFilterList("Sort By")
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
      }
    }
    step("Check Radio Button toggled, has no effect on Checkboxes, and 5 total selected filters listed at top of screen") {
      SortFilterScreen {
        findSelectedFilterItem(composeTestRule, "Older Reports")
        findSelectedFilterItem(composeTestRule, "Standard")
        findSelectedFilterItem(composeTestRule, "Isolation")
        findSelectedFilterItem(composeTestRule, "Contact")
        findSelectedFilterItem(composeTestRule, "Contact Enteric")
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(5)
        selectedFilterButton("Older Reports").assertIsDisplayed()
        selectedFilterButton("Standard").assertIsDisplayed()
        selectedFilterButton("Isolation").assertIsDisplayed()
        selectedFilterButton("Contact").assertIsDisplayed()
        selectedFilterButton("Contact Enteric").assertIsDisplayed()
      }
    }
  }

  //! Removing Filters by Tapping Again or Tapping SelectedFilterButton
  @Test fun remove_Selected_Filter_By_Re_Tapping() = run {
    step("Select Sort By Older Reports and Standard Precaution Type Filter") {
      SortFilterScreen {
        openExpandableFilterList("Sort By")
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
        openExpandableFilterList("Precaution Type")
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle()

        findSelectedFilterItem(composeTestRule, "Older Reports")
        findSelectedFilterItem(composeTestRule, "Standard") //? This func explicitly looks for ONE "Toggled ON" FilterItem
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(2)
        selectedFilterButton("Older Reports").assertIsDisplayed()
        selectedFilterButton("Standard").assertIsDisplayed()
      }
    }
    step("Remove filters by re-tapping them") {
      SortFilterScreen {
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle()

        findUnselectedFilterItem(composeTestRule, "Older Reports")
        findUnselectedFilterItem(composeTestRule, "Standard") //? This func explicitly looks for ONE "Toggled OFF" FilterItem
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(0)
        selectedFilterButton("Older Reports").assertDoesNotExist()
        selectedFilterButton("Standard").assertDoesNotExist()
      }
    }
  }
  @Test fun remove_Selected_Filter_By_Tapping_SelectedFilterButton() = run {
    step("Select Sort By Older Reports and Standard Precaution Type Filter") {
      SortFilterScreen {
        openExpandableFilterList("Sort By")
        tapFilterItem(composeTestRule, "Older Reports")
        device.uiDevice.waitForIdle()
        openExpandableFilterList("Precaution Type")
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle()

        findSelectedFilterItem(composeTestRule, "Older Reports")
        findSelectedFilterItem(composeTestRule, "Standard")
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButtons.assertCountEquals(2)
        selectedFilterButton("Older Reports").assertIsDisplayed()
        selectedFilterButton("Standard").assertIsDisplayed()
      }
    }
    step("Remove filters by tapping them in the SelectedFilterListView") {
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButton("Older Reports").performClick()
        device.uiDevice.waitForIdle()
        selectedFilterButton("Standard").performClick()
        device.uiDevice.waitForIdle()
        selectedFilterButtons.assertCountEquals(0)
      }
      SortFilterScreen.findUnselectedFilterItem(composeTestRule, "Older Reports")
      SortFilterScreen.findUnselectedFilterItem(composeTestRule, "Standard")
    }
  }

  //! Toolbar Features
  @Test fun reset_Filters_Chosen() = run {
    step("Select Standard Precaution Type and PPE Health Practice Type Filters") {
      SortFilterScreen {
        openExpandableFilterList("Precaution Type")
        //? Order is important for the remainder of this SortFilterScreen lambda
        tapFilterItem(composeTestRule, "Standard")
        device.uiDevice.waitForIdle() //? Each waitForIdle() helps ensure the upcoming findSelectedFilterItem() calls succeed!
        openExpandableFilterList("Health Practice Type")
        tapFilterItem(composeTestRule, "PPE")
        device.uiDevice.waitForIdle() //? SINCE, the filterRVs can be slow recycling their ComposeViewHolders causing
        //? 2 of the same Composable to exist, one in the OFF state and the other in the ON state
        findSelectedFilterItem(composeTestRule, "Standard")
        findSelectedFilterItem(composeTestRule, "PPE")
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButton("Standard").assertIsDisplayed()
        selectedFilterButton("PPE").assertIsDisplayed()
      }
    }
    step("Press resetFiltersButton and check ExpandableLists closed + SelectedFilters ALL toggled off") {
      SortFilterScreen.resetFiltersButton.click()
      device.uiDevice.waitForIdle()
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButton("Standard").assertDoesNotExist()
        selectedFilterButton("PPE").assertDoesNotExist()
      }
      SortFilterScreen.expandableFilterList("Precaution Type").filterListRv.isGone()
      SortFilterScreen.expandableFilterList("Health Practice Type").filterListRv.isGone()
      composeTestRule.onAllNodes(hasTestTag("FilterRow")).assertAll(isOff())
    }
  }
  @Test fun finalize_Filters_Chosen() = run {
    step("Select Hand Hygiene and PPE Health Practice Type Filters") {
      SortFilterScreen {
        openExpandableFilterList("Health Practice Type")
        tapFilterItem(composeTestRule, "Hand Hygiene")
        device.uiDevice.waitForIdle()
        tapFilterItem(composeTestRule, "PPE")
        device.uiDevice.waitForIdle()
        findSelectedFilterItem(composeTestRule, "Hand Hygiene")
        findSelectedFilterItem(composeTestRule, "PPE")
      }
      onComposeScreen<SortFilterComposeScreen>(composeTestRule) {
        selectedFilterButton("Hand Hygiene").assertIsDisplayed()
        selectedFilterButton("PPE").assertIsDisplayed()
      }
    }
    step("Finalize selected filters and check ReportListScreen received them") {
      SortFilterScreen.setFiltersButton.click()

      assertEquals(3, ReportListScreen.reportList.getSize())
      onComposeScreen<ReportListComposeScreen>(composeTestRule) {
        sortFilterButtons.assertCountEquals(2)
        sorterFilterButtons("Hand Hygiene").assertExists()
        sorterFilterButtons("PPE").assertExists()
      }
    }
  }
}