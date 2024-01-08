package edu.usc.nlcaceres.infectionprevention.screens

import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentMain
import edu.usc.nlcaceres.infectionprevention.R
import io.github.kakaocup.kakao.progress.KProgressBar
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import android.view.View
import androidx.test.espresso.contrib.DrawerMatchers
import com.kaspersky.kaspresso.testcases.api.scenario.Scenario
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.drawer.KDrawerView
import io.github.kakaocup.kakao.navigation.KNavigationView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.toolbar.KToolbar

object MainActivityScreen: KScreen<MainActivityScreen>() {
  override val layoutId: Int = R.layout.fragment_main
  override val viewClass: Class<*> = FragmentMain::class.java

  //! Toolbar + its Content
  val appToolbar = KToolbar { withId(R.id.home_toolbar) }
  val settingsButton = KButton { withContentDescription("Settings") }
  //! NavDrawer + its Content
  val navDrawer = KDrawerView { withId(R.id.navDrawer) }
  fun KDrawerView.isOpen() {
    matches { DrawerMatchers.isOpen() }
  }
  fun KDrawerView.isClosed() {
    matches { DrawerMatchers.isClosed() }
  }
  val navView = KNavigationView { withId(R.id.nav_view) }
  fun goToReportList() = navView.navigateTo(R.id.reportListFragment)
  fun goToStandardReportList() = navView.navigateTo(R.id.actionToReportListFragmentWithStandardFilter)
  fun goToIsolationReportList() = navView.navigateTo(R.id.actionToReportListFragmentWithIsolationFilter)
  //! Main View
  val mainProgressBar = KProgressBar { withId(R.id.app_progressbar) }
  val sorryMessageTV = KTextView { withId(R.id.sorryTextView) }
  val precautionRV = KRecyclerView(builder = { withId(R.id.precautionRV) }, itemTypeBuilder = { itemType(::PrecautionRvItem) })

  class PrecautionRvItem(matcher: Matcher<View>): KRecyclerItem<PrecautionRvItem>(matcher) {
    val precautionTypeTV = KTextView(matcher) { withId(R.id.precautionTypeTView) }
    val healthPracticeRV = KRecyclerView(matcher,
      { withId(R.id.horizontalRecycleView) }, { itemType(::ComposeHealthPracticeRvItem) }
    )

    fun healthPracticeItem(name: String) =
      healthPracticeRV.childWith<ComposeHealthPracticeRvItem> { withContentDescription("Create $name Report Button") }
    fun scrollToHealthPractice(name: String) { // Useful to guarantee ComposeRule can find the ComposeView's Content Nodes
      healthPracticeItem(name).scrollTo() // Since all HealthPracticeRV Children are ComposeViews
    }

    class HealthPracticeRvItem(matcher: Matcher<View>): KRecyclerItem<HealthPracticeRvItem>(matcher) {
      val container = KView(matcher) { withId(R.id.practiceItemView) }
    }
    class ComposeHealthPracticeRvItem(matcher: Matcher<View>): KRecyclerItem<HealthPracticeRvItem>(matcher) {
      /** All RecyclerView items that use Composables init their own ComposeView which each contain an AndroidComposeView
       * that represents/holds the actual Compose Content. BUT as of 2023, the Espresso Matchers and
       * ComposeRule/SemanticsNodeInteractionsProvider Matchers are not actually interoperable, i.e.
       * Espresso Matchers can't see the Compose SemanticsNodes and vice versa. As a result, you can't
       * be sure if the ComposeView's Content Root exists within this particular RecyclerViewItem's ComposeView */
    }
  }
}

//? Since Objects are a bit like Singletons/Static classes, they can't be initialized, so they're not great for holding state
//? UNLESS it's meant to be global (and STATIC). You can change an object's underlying singular global state by having the
//? object implement setters/data manipulation funcs BUT, at that point, a class is more likely to be the proper solution
class NavDrawerScenario(private val reportPath: MainActivityScreen.() -> Unit): Scenario() {
  override val steps: TestContext<Unit>.() -> Unit = {
    MainActivityScreen {
      navDrawer.isClosed()
      navDrawer.open()
      navDrawer.isOpen()
      navView.isVisible()
      reportPath()
    }
  }
}
