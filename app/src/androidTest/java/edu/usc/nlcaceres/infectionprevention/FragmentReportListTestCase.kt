package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.matcher.ViewMatchers
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import edu.usc.nlcaceres.infectionprevention.screens.NavDrawerScenario
import edu.usc.nlcaceres.infectionprevention.screens.ReportListScreen
import edu.usc.nlcaceres.infectionprevention.screens.SettingsScreen
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import io.github.kakaocup.kakao.edit.KEditText
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class FragmentReportListTestCase: TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createAndroidComposeRule<ActivityMain>()

  @Before fun navigateToReportList() {
    run { scenario(NavDrawerScenario { goToReportList() }) }
    ReportListScreen { sorryMessageTV.isInvisible(); assertEquals(5, reportList.getSize()) }
  }

  //! Navigation
  @Test fun navigate_To_Sort_Filter_Fragment() {
    ReportListScreen { sortFilterButton.click() }
    //TODO: FragmentSortFilter checks
  }
  @Test fun navigate_To_Settings() {
    ReportListScreen { settingsButton.click() }
    SettingsScreen {
      personalInfoTitle { hasText("Personal Info") }
      adminInfoTitle { isVisible() }
    }
  }
  @Test fun navigate_Back_To_Home_Page() {
    ReportListScreen { pressBack() }
    MainActivityScreen {
      assertEquals(2, precautionRV.getSize())
      precautionRV.lastChild<MainActivityScreen.PrecautionRvItem> {
        assertEquals(4, healthPracticeRV.getSize())
      }
    }
  }

  //! Searchbar Filtering
  @Test fun expand_SearchBar_ActionView_When_Empty() {
    ReportListScreen {
      searchButton.click() // Expands searchBar
      searchBar.isDisplayed()
      searchBar.hasText("")
      searchBar.isFocused()
      searchBarCloseButton.click() // Closes the searchBar via ActionBar collapse
      device.uiDevice.waitForIdle()
      searchBar.doesNotExist()

      searchButton.click() // Expands searchBar
      searchBar.isDisplayed()
      searchBar.hasText("")
      searchBar.isFocused()
      reportList.click() // WHEN tapping away from the searchBar
      searchBar.isDisplayed() // THEN it DOESN'T hide the searchBar
      searchBar.isFocused() // AND focus is retained!
    }
  }
  @Test fun expand_SearchBar_ActionView_With_Text() {
    run {
      step("Expand searchBar and check if displayed, focused, and empty") {
        ReportListScreen {
          searchButton.click()
          searchBar.isDisplayed()
          searchBar.hasText("")
          searchBar.isFocused()
        }
      }
      step("Enter 'USC' search term and get 2 reports") {
        ReportListScreen {
          searchBar.typeText("USC")
          device.uiDevice.waitForIdle() //? Useful when UI is running animations, especially ones you can't control like typing
          assertEquals(2, reportList.getSize())
        }
      }
      step("Can tap away from searchBar BUT it remains open until its close button tapped") {
        ReportListScreen {
          reportList.click()
          searchBar.isDisplayed()
          searchBarCloseButton.click()
          device.uiDevice.waitForIdle()
          searchBar.doesNotExist()
        }
      }
      step("Reopen searchBar after filling BUT find an empty searchbar") {
        ReportListScreen {
          searchButton.click()
          searchBar.isDisplayed()
          searchBar.hasText("")
        }
      }
    }
  }
  @Test fun filter_With_SearchBar() {
    run {
      step("Expand searchBar, check if empty and displayed, and check report list has 5 reports") {
        ReportListScreen {
          searchButton.click()
          searchBar.isDisplayed()
          searchBar.hasText("")
          assertEquals(5, reportList.getSize())
        }
      }
      step("Enter 'HSC' searchTerm and get 3 reports") {
        ReportListScreen {
          searchBar.typeText("HSC")
          device.uiDevice.waitForIdle()
          assertEquals(3, reportList.getSize())
        }
      }
      step("Close the searchBar and get 5 reports again") {
        ReportListScreen {
          searchBarCloseButton.click()
          device.uiDevice.waitForIdle()
          searchBar.doesNotExist()
          assertEquals(5, reportList.getSize())
        }
      }
      step("Reopen the searchBar, checking if its visible, empty, and especially focused so typing 'USC' just works") {
        ReportListScreen {
          searchButton.click()
          searchBar.isDisplayed()
          searchBar.hasText("") // WHEN re-opened, searchBar expected to be autofocused
          val expectedSearchBar = KEditText { withMatcher(ViewMatchers.isFocused()) }
          expectedSearchBar.typeText("USC") // THEN autofocused view will be searchBar allowing typing
          device.uiDevice.waitForIdle()
          assertEquals(2, reportList.getSize())
          searchBarCloseButton.click()
        }
      }
    }
  }

  //TODO: Add FragmentSortFilter's FilterList result testing
}