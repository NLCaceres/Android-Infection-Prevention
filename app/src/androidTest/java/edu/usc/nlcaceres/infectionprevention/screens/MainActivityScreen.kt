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

object MainActivityScreen: KScreen<MainActivityScreen>() {
    override val layoutId: Int = R.layout.fragment_main
    override val viewClass: Class<*> = FragmentMain::class.java

    val mainProgressBar = KProgressBar { withId(R.id.app_progressbar) }
    val sorryMessageTV = KTextView { withId(R.id.sorryTextView) }
    val precautionRV = KRecyclerView(builder = { withId(R.id.precautionRV) }, itemTypeBuilder = { itemType(::PrecautionRvItem) })

    class PrecautionRvItem(matcher: Matcher<View>): KRecyclerItem<PrecautionRvItem>(matcher) {
        val precautionTypeTV = KTextView(matcher) { withId(R.id.precautionTypeTView) }
    }
}