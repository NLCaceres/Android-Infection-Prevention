package edu.usc.nlcaceres.infectionprevention.util

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.usc.nlcaceres.infectionprevention.ActivitySettings
import edu.usc.nlcaceres.infectionprevention.ActivitySortFilter
import edu.usc.nlcaceres.infectionprevention.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import edu.usc.nlcaceres.infectionprevention.helpers.util.withDrawable

@Config(instrumentedPackages = ["androidx.loader.content"]) // Needed to get final members on androidx.loader.content.ModernAsyncTask (Fixed by Espresso 3.5)
@RunWith(AndroidJUnit4::class) // AndroidJUnit4Runner works thanks to Robolectric in UnitTests (Eliminating need to use RobolectricTestRunner)
class ViewHelperTest {

  @Test fun progressIndicatorVisibility() {
    // Why not ApplicationProvider.getApplicationContext?
    // Following is a bit easier AndroidX wants context to infer Context type (activity, application, etc.)
    // val anotherContext: Context = getApplicationContext() // OR getApplicationContext<Context>()
    val mockContext: Context = RuntimeEnvironment.getApplication()
    val progressBar = ProgressBar(mockContext)
    HideProgressIndicator(false, progressBar)
    assertEquals(progressBar.visibility, View.VISIBLE)
    HideProgressIndicator(true, progressBar)
    assertEquals(progressBar.visibility, View.INVISIBLE)
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