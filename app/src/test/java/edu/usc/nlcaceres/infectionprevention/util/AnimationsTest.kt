package edu.usc.nlcaceres.infectionprevention.util

import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.usc.nlcaceres.infectionprevention.InfectionProtectionApplication
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AnimationsTest {
  /* Unfortunately testing animations is limited to starting values and ending values since Robolectric is fairly limited
  * UI Tests can handle testing animations a bit better but are also still fairly limited and more black-boxy */
  @Test
  fun testFlashingAnimation() {
    val mockContext = getApplicationContext<InfectionProtectionApplication>()
    val imageView = ImageView(mockContext)
    assertEquals(View.VISIBLE, imageView.visibility) // Robolectric starts views as VISIBLE & 1.0f alpha
    assertEquals(1.0f, imageView.alpha)

    createFlashingAnimation(imageView).start()
    assertEquals(View.VISIBLE, imageView.visibility)
    assertEquals(0.0f, imageView.alpha)

    // Robolectric Shadow on MainLooper lets robolectric make unit tests wait for animator to finish
    // ALSO No need for @LooperMode annotation since Robolectric defaults to Looper.Mode.PAUSED which
    // allows the rest to work since PAUSED mode mimics Android's looper better than its original LEGACY mode
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(100L, TimeUnit.MILLISECONDS)

    // Debug version does NOT use ReverseMode so it's a simple 0.0f to 1.0f fade in
    assertEquals(1.0f, imageView.alpha)
    assertEquals(View.GONE, imageView.visibility) // Then onEnd quickly shifts to GONE
  }
}