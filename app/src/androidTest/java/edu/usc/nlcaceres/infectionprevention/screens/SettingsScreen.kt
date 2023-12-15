package edu.usc.nlcaceres.infectionprevention.screens

import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentSettings
import android.R as AndroidRes
import androidx.preference.R as PrefRes
import org.hamcrest.Matcher
import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView

object SettingsScreen: KScreen<SettingsScreen>() {
  override val layoutId = null //? Null since FragmentSettings is init'd + laid out via the Preference Library
  override val viewClass = FragmentSettings::class.java

  val recyclerView = KRecyclerView(
    builder = { withId(PrefRes.id.recycler_view) }, itemTypeBuilder = { itemType(::PreferenceItem) }
  )
  class PreferenceItem(matcher: Matcher<View>): KRecyclerItem<PreferenceItem>(matcher) {
    val title = KTextView(matcher) { withId(AndroidRes.id.title) }
    val summary = KTextView(matcher) { withId(AndroidRes.id.summary) }
  }
}