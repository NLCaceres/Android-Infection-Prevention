package edu.usc.nlcaceres.infectionprevention.util

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.usc.nlcaceres.infectionprevention.ActivitySettings
import edu.usc.nlcaceres.infectionprevention.ActivitySortFilter
import edu.usc.nlcaceres.infectionprevention.InfectionProtectionApplication
import edu.usc.nlcaceres.infectionprevention.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import edu.usc.nlcaceres.infectionprevention.helpers.util.withDrawable

// Need @Config to get final members on androidx.loader.content.ModernAsyncTask in setupToolbar test (Fixed by Espresso 3.5)
@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(AndroidJUnit4::class) // AndroidJUnit4Runner works thanks to Robolectric in UnitTests (Eliminating need to use RobolectricTestRunner)
class ViewHelperTest {

  @Test fun checkDarkMode() {
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

  @Test fun progressIndicatorVisibility() {
    // val mockContext: Context = RuntimeEnvironment.getApplication() // Easier BUT semi-deprecated SINCE
    val mockContext = getApplicationContext<InfectionProtectionApplication>() // AndroidX prefers to infer Context via activity, application, etc.
    val progressBar = ProgressBar(mockContext)
    HideProgressIndicator(false, progressBar)
    assertEquals(View.VISIBLE, progressBar.visibility)
    HideProgressIndicator(true, progressBar)
    assertEquals(View.INVISIBLE, progressBar.visibility)
  }

  @Test fun setupToolbar() { // Use specific activities as examples of SetupToolbar to test
    launch(ActivitySettings::class.java).use { // Use() {} Ensures activity closes
      onView(withId(R.id.home_toolbar)).check(matches(isDisplayed()))
      onView(withId(R.id.home_toolbar)).check(matches(withChild(withId(R.id.toolbar_logo))))
    }
    launch(ActivitySortFilter::class.java).use { // Check if Up indicator changed
      onView(withId(R.id.home_toolbar)).check(matches(isDisplayed()))
      // The toolbar's Up Indicator uses an ImageView in the button view
      onView(withContentDescription(R.string.abc_action_bar_up_description)).
        check(matches(isDisplayed())).check(matches(withDrawable(R.drawable.ic_close)))
    }
  }
}