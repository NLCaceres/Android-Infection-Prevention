package edu.usc.nlcaceres.infectionprevention.robots

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withId
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.helpers.util.withSibling
import edu.usc.nlcaceres.infectionprevention.helpers.util.isOnScreen
import edu.usc.nlcaceres.infectionprevention.helpers.util.hasText
import edu.usc.nlcaceres.infectionprevention.helpers.util.matching
import edu.usc.nlcaceres.infectionprevention.helpers.util.isNotInLayout
import edu.usc.nlcaceres.infectionprevention.helpers.util.eraseText
import edu.usc.nlcaceres.infectionprevention.helpers.util.updateText
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.swipeToLabeled
import edu.usc.nlcaceres.infectionprevention.helpers.util.tapItemLabeled
import org.hamcrest.Matchers.allOf

class SettingsRobot: BaseRobot() {

  fun checkInitLoad() {
    personalInfoHeader().isOnScreen()
    adminInfoHeader().isOnScreen()
  }

  fun findPreference(title: String, summary: String? = null) {
    preferenceRV().isOnScreen()
    goToPreference(title)
    preferenceTitle(title).isOnScreen()
    summary?.let { preferenceSummary(it, title).isOnScreen() }
  }
  fun openPreferenceEditDialog(title: String) = tapPreference(title)
  fun findAndOpenPreference(title: String) {
    goToPreference(title)
    tapPreference(title)
  }
  fun checkDialogTitle(title: String) = dialogTitle(title).isOnScreen()
  fun checkDialogEditText(value: String? = null, hint: String? = null) {
    dialogEditText().isOnScreen()
    value?.let { dialogEditText().hasText(it) }
    hint?.let { dialogEditText().matching(withHint(it)) }
  }
  fun checkDialogNotLoaded(title: String) {
    dialogTitle(title).isNotInLayout()
    dialogCancelButton().isNotInLayout()
    dialogOkButton().isNotInLayout()
  }
  fun checkDialogLoaded(title: String, value: String? = null, hint: String? = null) {
    checkDialogTitle(title)
    checkDialogEditText(value, hint)
    dialogCancelButton().isOnScreen()
    dialogOkButton().isOnScreen()
  }
  // Replace the text rather than use write that would just add on to a previous pref text value
  fun eraseOldPrefValue() { dialogEditText().eraseText() }
  fun enterNewPrefValue(text: String) { dialogEditText().updateText(text) }
  fun dismissDialog() = dialogCancelButton().tap()
  fun tapDialogOkButton() = dialogOkButton().tap()

  companion object {
    fun personalInfoHeader(): ViewInteraction = onView(withText("Personal Info"))
    fun adminInfoHeader(): ViewInteraction = onView(withText("Hospital-wide Admin Settings"))

    fun preferenceRV(): ViewInteraction = onView(withId(R.id.recycler_view))
    fun goToPreference(title: String): ViewInteraction = preferenceRV().swipeToLabeled<RecyclerView.ViewHolder>(title)
    fun tapPreference(title: String): ViewInteraction = preferenceRV().tapItemLabeled<RecyclerView.ViewHolder>(title)
    fun preferenceTitle(title: String): ViewInteraction = onView(allOf(withId(android.R.id.title), withText(title)))
    fun preferenceSummary(summary: String, title: String): ViewInteraction = onView(allOf(withId(android.R.id.summary),
      withText(summary), withSibling(title)))

    fun dialogTitle(title: String): ViewInteraction = onView(allOf(withId(android.R.id.title), withText(title),
      withSibling(R.id.alertCancelButton), withSibling(R.id.alertOkButton)))
    fun dialogEditText(): ViewInteraction = onView(withId(android.R.id.edit))
    fun dialogCancelButton(): ViewInteraction = onView(withId(R.id.alertCancelButton))
    fun dialogOkButton(): ViewInteraction = onView(withId(R.id.alertOkButton))
  }
}