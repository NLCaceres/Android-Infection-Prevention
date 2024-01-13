package edu.usc.nlcaceres.infectionprevention.composables.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.usc.nlcaceres.infectionprevention.composables.listItems.OnRemoveSelectedFilter
import edu.usc.nlcaceres.infectionprevention.composables.listItems.SelectedFilterList
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelSortFilter

/** Used in FragmentSortFilter to visualize selected sorters and filters
 * Selected sorters and filters can be removed on tap
**/
//? Technically, the viewModel is the ONLY unstable param in this file BUT since it's only really used to pass
//? the Stable `selectedFilterList` into SelectedFilterList, it DOESN'T seem to trigger SelectedFilterListFragment to also recompose
@Composable
fun SelectedFilterListView(modifier: Modifier = Modifier, viewModel: ViewModelSortFilter = viewModel(), onClick: OnRemoveSelectedFilter) {
  SelectedFilterList(viewModel.selectedFilterList, modifier.then(Modifier.testTag("SelectedFilterListView"))) { position, filter ->
    viewModel.removeSelectedFilter(position) // First remove filter from selectedFilterList
    onClick(position, filter) // THEN let Parent View of the ComposeView containing this SelectedFilterListFrag run its own callback
  }
}
