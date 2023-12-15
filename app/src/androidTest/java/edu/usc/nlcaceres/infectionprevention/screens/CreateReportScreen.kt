package edu.usc.nlcaceres.infectionprevention.screens

import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentCreateReport
import edu.usc.nlcaceres.infectionprevention.R
import io.github.kakaocup.kakao.text.KTextView

object CreateReportScreen: KScreen<CreateReportScreen>() {
  override val layoutId = R.layout.fragment_create_report
  override val viewClass = FragmentCreateReport::class.java

  val headerTV = KTextView { withId(R.id.headerTV) }
}