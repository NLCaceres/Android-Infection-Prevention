package edu.usc.nlcaceres.infectionprevention.screens

import android.view.View
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.performClick
import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentSortFilter
import edu.usc.nlcaceres.infectionprevention.R
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object SortFilterScreen: KScreen<SortFilterScreen>() {
  override val layoutId = R.layout.fragment_sort_filter
  override val viewClass = FragmentSortFilter::class.java

  //! Actionbar Menu Buttons
  val settingsButton = KButton { withContentDescription("Settings") }
  val setFiltersButton = KButton { withId(R.id.set_filters_action) }
  val resetFiltersButton = KButton { withId(R.id.reset_filters_action) }

  //! Main View
  val expandableFilterList = KRecyclerView(builder = { withId(R.id.expandableFilterRecyclerView) }, itemTypeBuilder = { itemType(::ExpandableListItem) })
  class ExpandableListItem(matcher: Matcher<View>): KRecyclerItem<ExpandableListItem>(matcher) {
    val filterGroupName = KTextView(matcher) { withId(R.id.filterGroupNameTextView) }
    val filterListRv = KRecyclerView(matcher, { withId(R.id.filterRecyclerView) }, { itemType(::ComposeFilterItem) })
  }
  //? No body needed for the this class, since Compose handles the view's content
  class ComposeFilterItem(matcher: Matcher<View>): KRecyclerItem<ComposeFilterItem>(matcher)

  fun expandableFilterList(text: String) =
    expandableFilterList.childWith<ExpandableListItem> { withDescendant { containsText(text) } }
  fun openExpandableFilterList(text: String) {
    expandableFilterList(text).click()
  }
  //! Actually the Composables in the ComposeFilterItem of the ExpandableFilterRV. NOT the Compose half of the screen!
  fun findFilterItem(semanticsProvider: SemanticsNodeInteractionsProvider, text: String) =
    semanticsProvider.onNode(hasTestTag("FilterRow") and hasText(text))
  fun findSelectedFilterItem(semanticsProvider: SemanticsNodeInteractionsProvider, text: String) =
    semanticsProvider.onNode(hasTestTag("FilterRow") and hasText(text) and isOn())
  fun findUnselectedFilterItem(semanticsProvider: SemanticsNodeInteractionsProvider, text: String) =
    semanticsProvider.onNode(hasTestTag("FilterRow") and hasText(text) and isOff())
  fun tapFilterItem(semanticsProvider: SemanticsNodeInteractionsProvider, text: String) =
    findFilterItem(semanticsProvider, text).performClick()
}

//! This is the Compose half of the screen. Handling the list of SELECTED_FILTERS
class SortFilterComposeScreen(semanticsProvider: SemanticsNodeInteractionsProvider):
  ComposeScreen<SortFilterComposeScreen>(semanticsProvider, viewBuilderAction = { hasTestTag("SelectedFilterListView") }) {
  fun selectedFilterButton(text: String): KNode = child { hasText(text) }
  val selectedFilterButtons = semanticsProvider.onAllNodes(hasParent(hasTestTag("SelectedFilterListView")))
}