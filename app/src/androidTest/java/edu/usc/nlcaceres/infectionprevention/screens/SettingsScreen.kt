package edu.usc.nlcaceres.infectionprevention.screens

import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentSettings
import edu.usc.nlcaceres.infectionprevention.R
import android.R as AndroidRes
import androidx.preference.R as PrefRes
import org.hamcrest.Matcher
import android.view.View
import io.github.kakaocup.kakao.common.builders.ViewBuilder
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matchers.allOf
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId as usingId

object SettingsScreen: KScreen<SettingsScreen>() {
  override val layoutId = null //? Null since FragmentSettings is init'd + laid out via the Preference Library
  override val viewClass = FragmentSettings::class.java

  val recyclerView = KRecyclerView(
    builder = { withId(PrefRes.id.recycler_view) }, itemTypeBuilder = { itemType(::PreferenceItem) }
  )
  fun personalInfoTitle(assertions: KTextView.() -> Unit) { recyclerView.firstChild<PreferenceItem> { title { assertions() } } }
  //? These two funcs above and below are actually exactly the same. Under the hood, Kakao operator
  //? overloads invoke(), which lets KViews run lambdas like a DSL, i.e. `someView { itsFunc() }`
  fun adminInfoTitle(assertions: KTextView.() -> Unit) {
    recyclerView.childWith<PreferenceItem> { withDescendant { withText("Hospital-wide Admin Settings") } }
      .title.invoke(assertions)
  }
  // Wrapping with `withDescendant { }` makes finding a particular Preference in the Settings List super simple
  fun recyclerViewChild(matcher: ViewBuilder.() -> Unit) =
    recyclerView.childWith<PreferenceItem> { withDescendant { matcher() } }

  // Technically this AlertDialog helper works BUT its child views don't use my custom IDs
  // val dialogView = KAlertDialog() // Since my AppFragmentAlertDialog is used to imitate the normal/common AlertDialog
  val dialogView = KView { withMatcher(allOf( // Using withMatcher(allOf()) avoids multiple matching views issue
    withChild(usingId(AndroidRes.id.title)), withChild(usingId(R.id.alertOkButton)), withChild(usingId(R.id.alertCancelButton))
  )) }
  val dialogTitle = KTextView { withId(AndroidRes.id.title) }
  val dialogET = KEditText { withId(AndroidRes.id.edit) }
  val dialogOkButton = KButton { withId(R.id.alertOkButton) }
  val dialogCancelButton = KButton { withId(R.id.alertCancelButton) }
}

class PreferenceItem(matcher: Matcher<View>): KRecyclerItem<PreferenceItem>(matcher) {
  val title = KTextView(matcher) { withId(AndroidRes.id.title) }
  val summary = KTextView(matcher) { withId(AndroidRes.id.summary) }
}