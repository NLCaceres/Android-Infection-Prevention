package edu.usc.nlcaceres.infectionprevention.composables.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelSortFilter

typealias OnRemoveSelectedFilter = (filter: FilterItem, position: Int) -> Unit

@Composable
fun SelectedFilter(filter: FilterItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
  // Touch target MIGHT be too big for Filter Removal BUT making the Image/Icon the touch target may be too small
  Button(onClick, Modifier.then(modifier), shape = RoundedCornerShape(40.dp),
    colors = ButtonDefaults.buttonColors(Color.Gray), border = BorderStroke(2.dp, Color.Black), contentPadding = PaddingValues(0.dp)
  ) {
    Image(painterResource(R.drawable.ic_close), stringResource(R.string.remove_filter_button),
      Modifier.padding(4.dp).background(colorResource(R.color.colorPrimary), RoundedCornerShape(20.dp))
        .border(2.dp, Color.Black, RoundedCornerShape(40.dp)))
    Text(filter.name, Modifier.padding(start = 8.dp, end = 16.dp).align(Alignment.CenterVertically),
      Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedFilterList(selectedFilters: List<FilterItem>, modifier: Modifier = Modifier, onClickFilter: OnRemoveSelectedFilter) {
  // Difference between Row and FlowRow is that a FlowRow will grow in height, effectively making multiple rows
  // Rows cut off any items that exceed the Row's width while LazyRows become scrollable in the x-direction
  FlowRow(Modifier.then(modifier), Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally), Arrangement.spacedBy(3.dp)) {
    selectedFilters.forEachIndexed { position, selectedFilter ->
      // Since the `selectedFilters` param is received from a StateFlow, which treats every emitted value as
      // a totally new list, ALL Flow Row items ALWAYS recompose, and there's seemingly no way around it
      // SO key() here only helps to ensure onClick fully runs even if recomposition starts
      key(selectedFilter.filterGroupName to selectedFilter.name) {
        SelectedFilter(selectedFilter, onClick = { onClickFilter(selectedFilter, position) })
      }
    }
  }
}
@Composable
fun SelectedFilterListFragment(modifier: Modifier = Modifier, viewModel: ViewModelSortFilter = viewModel(), onClick: OnRemoveSelectedFilter) {
  val someList by viewModel.selectedFilterList.collectAsStateWithLifecycle()
  SelectedFilterList(someList, modifier) { filter, position ->
    viewModel.removeSelectedFilter(position) // First remove filter from selectedFilterList
    onClick(filter, position) // THEN let Parent View of the ComposeView containing this SelectedFilterListFrag run its own callback
  }
}

@Preview(widthDp = 325, showBackground = true)
@Composable
fun SelectedFilterPreview() {
  SelectedFilterList(listOf(
    FilterItem("Filter Name 1", false, "Filter Group"),
    FilterItem("Filter Name 2", false, "Filter Group"),
    FilterItem("Filter Name 3", false, "Filter Group"),
    FilterItem("Filter Name 4", false, "Filter Group"),
    FilterItem("Filter Name 5", false, "Filter Group"),
  )) { _, _ -> }
}

