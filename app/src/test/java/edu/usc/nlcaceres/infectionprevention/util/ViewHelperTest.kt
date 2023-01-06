package edu.usc.nlcaceres.infectionprevention.util

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.usc.nlcaceres.infectionprevention.InfectionProtectionApplication
import edu.usc.nlcaceres.infectionprevention.ActivityMain
import edu.usc.nlcaceres.infectionprevention.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import edu.usc.nlcaceres.infectionprevention.helpers.util.withDrawable

// Need @Config to get final members on androidx.loader.content.ModernAsyncTask in setupToolbar test (Fixed by Espresso 3.5)
@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(AndroidJUnit4::class) // AndroidJUnit4Runner works thanks to Robolectric in UnitTests (Eliminating need to use RobolectricTestRunner)
class ViewHelperTest {

  @Test fun `Check Dark Mode Updates Correctly`() {
    val app = getApplicationContext<InfectionProtectionApplication>()
    // Currently app starts as not in dark mode
    val notDarkMode = IsDarkMode(app)
    assertEquals(false, notDarkMode) // So we get back false, we are NOT in dark mode

    // THEN we change the app to darkMode
    app.resources.configuration.uiMode = UI_MODE_NIGHT_YES // UI_MODE_NIGHT_YES == decimal 32 (UI_MODE_NIGHT_NO == 16)
    // The func applies the bitwise AND w/ the expected UI_MODE_NIGHT_MASK (aka decimal 48) (48 & 32 == 32 aka UI_MODE_NIGHT_YES)
    val isDarkMode = IsDarkMode(app)
    assertEquals(true, isDarkMode) // So now that we've changed it, we get darkMode == true!
  }

  @Test fun `Check Strings are Properly Snakecased`() {
    val blankResult = SnakecaseString("")
    assertEquals("", blankResult)
    val noEffectLowercasedResult = SnakecaseString("hello")
    assertEquals("hello", noEffectLowercasedResult)
    val simpleLowercasedResult = SnakecaseString("Hello")
    assertEquals("hello", simpleLowercasedResult)

    val twoUpperToLowercasedResult = SnakecaseString("Hello World")
    assertEquals("hello_world", twoUpperToLowercasedResult)
    val oneUpperToLowercasedWordResult = SnakecaseString("Hello world")
    assertEquals("hello_world", oneUpperToLowercasedWordResult)
    val noUpperToLowercasedWordResult = SnakecaseString("hello world")
    assertEquals("hello_world", noUpperToLowercasedWordResult)

    val multiWordResult = SnakecaseString("The World is Great")
    assertEquals("the_world_is_great", multiWordResult)

    val camelCaseToSnakeCase = SnakecaseString("TheWorldIsGreat") // DOESN'T convert camelcase
    assertNotEquals("the_world_is_great", camelCaseToSnakeCase)
    assertEquals("theworldisgreat", camelCaseToSnakeCase)
  }
  @Test fun `Check Strings Combined into a Proper Transition Name`() {
    val blankTransitionName = TransitionName("", "")
    assertEquals(".", blankTransitionName) // Returns simple dot (since nothing to combine)
    val blankID = TransitionName("prefix", "")
    assertEquals("prefix.", blankID)
    val blankPrefix = TransitionName("", "some id")
    assertEquals(".some_id", blankPrefix)

    val typicalResult = TransitionName("prefix", "something")
    assertEquals("prefix.something", typicalResult)
    val typicalLongPrefixResult = TransitionName("some.long.prefix", "something")
    assertEquals("some.long.prefix.something", typicalLongPrefixResult)
    val typicalLongPrefixAndIdResult = TransitionName("some.long.prefix", "some id")
    assertEquals("some.long.prefix.some_id", typicalLongPrefixAndIdResult)
    val typicalSnakecasedPrefixAndLongId = TransitionName("some_thing.long.pre_fix", "Some Longer Id")
    assertEquals("some_thing.long.pre_fix.some_longer_id", typicalSnakecasedPrefixAndLongId)
  }

  @Test fun `Check Progress Indicator Visibility Updates Correctly`() {
    // val mockContext: Context = RuntimeEnvironment.getApplication() // Easier BUT semi-deprecated SINCE
    val mockContext = getApplicationContext<InfectionProtectionApplication>() // AndroidX prefers to infer Context via activity, application, etc.
    val progressBar = ProgressBar(mockContext)
    HideProgressIndicator(false, progressBar)
    assertEquals(View.VISIBLE, progressBar.visibility)
    HideProgressIndicator(true, progressBar)
    assertEquals(View.INVISIBLE, progressBar.visibility)
  }

  @Test fun `Check Up Indicator Updated Correctly`() {
    launch(ActivityMain::class.java).use {
      onView(withId(R.id.home_toolbar)).check(matches(isDisplayed()))
      onView(withId(R.id.home_toolbar)).check(matches(withChild(withId(R.id.toolbar_logo))))
      onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).
        check(matches(isDisplayed())).check(matches(withDrawable(R.drawable.ic_menu)))

      // Go to settings and check if toolbar updated up indicator
      onView(withId(R.id.home_toolbar)).check(matches(isDisplayed()))
      onView(withContentDescription("Settings")).perform(click())
      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).
        check(matches(isDisplayed())).check(matches(withDrawable(R.drawable.ic_back_arrow)))
    }
  }
}