package edu.usc.nlcaceres.infectionprevention.composables.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.usc.nlcaceres.infectionprevention.composables.listItems.OnRemoveSelectedFilter
import edu.usc.nlcaceres.infectionprevention.composables.listItems.SelectedFilterList
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelReportList

/** Used in FragmentReportList to visualize sorters and filters chosen in FragmentSortFilter
 * Once set, they are used to sort and filter the Reports
 * Chosen sorters and filters can be removed on tap
**/
@Composable
fun SorterFilterListView(modifier: Modifier = Modifier, viewModel: ViewModelReportList = viewModel(), onClick: OnRemoveSelectedFilter) {
  SelectedFilterList(viewModel.selectedFilters, modifier.then(Modifier.testTag("SorterFilterListView"))) { position, filter ->
    viewModel.selectedFilters.removeAt(position) // First remove filter from selectedFilter List
    onClick(position, filter) // THEN let Parent View of the ComposeView containing this SorterFilterListView run its own callback
  }
}