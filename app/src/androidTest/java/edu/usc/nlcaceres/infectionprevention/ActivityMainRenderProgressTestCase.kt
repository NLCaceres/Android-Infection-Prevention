package edu.usc.nlcaceres.infectionprevention

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario.launch
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakePrecautionRepository
import edu.usc.nlcaceres.infectionprevention.screens.MainActivityScreen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
class ActivityMainRenderProgressTestCase: TestCase() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val composeTestRule = createComposeRule()

  @Inject // Need @Inject to get the mutable stub Repository, THEN complete the injection in @Before
  lateinit var precautionRepository: PrecautionRepository

  @Before // Performs manual injection into the TestSuite itself, allowing each test to get a fresh injection
  fun injectDependencies() {
    hiltRule.inject()
  }

  @Test fun view_Loading() {
    // Still need a delay to prolong loading state
    (precautionRepository as FakePrecautionRepository).needDelay = true
    // BUT also need to remember that an emptyList ensures the sorryMsgTV is visible
    (precautionRepository as FakePrecautionRepository).someList = emptyList()

    launch(ActivityMain::class.java).use {
      MainActivityScreen {
        sorryMessageTV {
          isVisible() // SHOW the sorryMsg ONLY if no data (empty list) is received
          containsText("Looking up precautions")
        }
        mainProgressBar {
          isVisible()
        }
      }
    }
  }

  @Test fun view_Receives_No_Data_And_Unexpectedly_Catches_Error() {
    // WHEN expecting a list of data but not receiving any due to error caught
    (precautionRepository as FakePrecautionRepository).optionalClosure = { throw Exception("Error!") }
    (precautionRepository as FakePrecautionRepository).someList = emptyList()

    launch(ActivityMain::class.java).use {
      MainActivityScreen { // THEN err probably thrown, so render error message
        sorryMessageTV.isVisible()
        sorryMessageTV.hasText("Sorry! Seems we're having an issue on our end!")
        mainProgressBar.isInvisible()
      }
    }
  }
  @Test fun view_Unexpectedly_Has_Network_Issues() {
    // WHEN expecting a list of data but not receiving any due to Network Error
    (precautionRepository as FakePrecautionRepository).optionalClosure = { throw IOException("Error!") }
    (precautionRepository as FakePrecautionRepository).someList = emptyList()

    launch(ActivityMain::class.java).use {
      MainActivityScreen { // THEN render network error message
        sorryMessageTV.isVisible()
        sorryMessageTV.hasText("Sorry! Having trouble with the internet connection!")
        mainProgressBar.isInvisible()
      }
    }
  }

  @Test fun view_Receives_No_Data() {
    // WHEN expecting a list of data but receiving an empty list
    (precautionRepository as FakePrecautionRepository).someList = emptyList()
    launch(ActivityMain::class.java).use {
      MainActivityScreen { // THEN render a "No precautions found" message
        sorryMessageTV.isVisible()
        sorryMessageTV.hasText("Weird! Seems we don't have any available precautions to choose from!")
        mainProgressBar.isInvisible()
      }
    }
  }
  @Test fun view_Receives_Some_Data() {
    // WHEN expecting a list of data and receiving it
    launch(ActivityMain::class.java).use {
      MainActivityScreen { // THEN hide any error messages
        sorryMessageTV.isGone()
        mainProgressBar.isInvisible()
        // AND show the precaution sections, each with a "Create New" title textView
        assertEquals(2, precautionRV.getSize())
        precautionRV.firstChild<MainActivityScreen.PrecautionRvItem> {
          precautionTypeTV.hasText("Create a New Standard Report")
        }
      }
    }
  }
}