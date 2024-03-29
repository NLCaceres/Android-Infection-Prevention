package edu.usc.nlcaceres.infectionprevention.screens

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentReportList
import edu.usc.nlcaceres.infectionprevention.R
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ReportListScreen: KScreen<ReportListScreen>() {
  override val layoutId = R.layout.fragment_report_list
  override val viewClass = FragmentReportList::class.java

  val settingsButton = KButton { withContentDescription("Settings") }
  val searchButton = KButton { withId(R.id.search_action) }
  val searchBar = KEditText { withMatcher(ViewMatchers.withHint(R.string.search_hint)) }
  val searchBarCloseButton = KButton { withContentDescription("Collapse") }

  val sorryMessageTV = KTextView { withId(R.id.sorryTextView) }
  val reportList = KRecyclerView(builder = { withId(R.id.reportRV) }, itemTypeBuilder = { itemType(::ReportRvItem) })

  val sortFilterButton = KButton { withId(R.id.sortFilterFloatingButton) }

  class ReportRvItem(matcher: Matcher<View>): KRecyclerItem<ReportRvItem>(matcher) {
    val reportTypeTitleTV = KTextView(matcher) { withId(R.id.reportTypeTitleTV) }
    val dateTV = KTextView(matcher) { withId(R.id.reportDateTV) }
    val employeeNameTV = KTextView(matcher) { withId(R.id.reportEmployeeNameTV) }
    val locationTV = KTextView(matcher) { withId(R.id.reportLocationTV) }
  }
}

//! KakaoCompose is still very much a work in progress. It works well enough! BUT is missing a good chunk of the features
// of ComposeTestRule, making KakaoCompose a bit limiting, in particular when considering Rows, Columns, LazyRows, etc
// BUT I also think that Jetpack Compose's Testing API, itself, is part of the issue
class ReportListComposeScreen(semanticsProvider: SemanticsNodeInteractionsProvider):
  ComposeScreen<ReportListComposeScreen>(semanticsProvider, viewBuilderAction = { hasTestTag("SorterFilterListView") }) {
  fun sorterFilterButtons(text: String): KNode = child { hasText(text) }
  val sortFilterButtons = semanticsProvider.onAllNodes(hasParent(hasTestTag("SorterFilterListView")))
}