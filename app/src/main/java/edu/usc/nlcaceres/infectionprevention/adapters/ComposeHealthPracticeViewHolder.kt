package edu.usc.nlcaceres.infectionprevention.adapters

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.composables.items.HealthPracticeCreateReportButton
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice

/** Used in HealthPracticeAdapter to allow Composables to be used in FragmentMain's HealthPracticeRecyclerView Items */

class ComposeHealthPracticeViewHolder(private val composeView: ComposeView): RecyclerView.ViewHolder(composeView) {
  fun bind(healthPractice: HealthPractice, listener: HealthPracticeClickListener) {
    composeView.contentDescription = "Create ${healthPractice.name} Report Button"
    composeView.setContent {
      HealthPracticeCreateReportButton(healthPractice.name, Modifier.padding(top = 10.dp)) {
        listener.onHealthPracticeItemClick(composeView, healthPractice)
      }
    }
  }
}