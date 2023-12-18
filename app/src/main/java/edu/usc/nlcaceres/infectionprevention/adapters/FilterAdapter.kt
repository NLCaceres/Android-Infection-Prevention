package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import edu.usc.nlcaceres.infectionprevention.data.FilterItem

/* RecyclerView Adapter used to select different types of filters each contained in an expandable/accordion view
* that is multi-choice by default (typical checkbox behavior) and single choice if enabled
* Linked to: SelectedFilterAdapter through its parent ExpandableFilterAdapter */
class FilterAdapter(private val singleSelectionEnabled : Boolean, private val filterSelectedListener : OnFilterSelectedListener,
                    private val parentListener: FilterGroupListener) : ListAdapter<FilterItem, ComposeFilterViewHolder>(FilterDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ComposeFilterViewHolder(ComposeView(parent.context), singleSelectionEnabled, filterSelectedListener, parentListener) selector@{
      val currentlyCheckmarkedFilterIndex = currentList.indexOfFirst { it.isSelected }
      if (currentlyCheckmarkedFilterIndex == -1) { return@selector } // Nothing currently selected, so need to deselect anything, move on.
      currentList[currentlyCheckmarkedFilterIndex].isSelected = false // Unmark it
      notifyItemChanged(currentlyCheckmarkedFilterIndex)
      /* An alternative to the parentListener + singleSelectionHandler is a SortedList, efficiently notifying the RecyclerViewAdapter
      * of changes, commonly via the SortedListAdapterCallback implementation, similar to DiffUtil.ItemCallback BUT w/ added sorting! */
    } // The sorting aspect isn't needed here BUT it could be useful in the ReportList RV

  // Why not bind the listeners here? Because it would happen EVERY TIME the user scrolls and views are recycled, so ONLY BIND THE DATA
  override fun onBindViewHolder(holder: ComposeFilterViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class ComposeFilterViewHolder(private val composeView: ComposeView, private val singleSelectionEnabled: Boolean,
                              private val filterSelectedListener : OnFilterSelectedListener, private val parentListener: FilterGroupListener,
                              private val handleSingleSelection: () -> Unit): RecyclerView.ViewHolder(composeView) {
  fun bind(filter: FilterItem) {
    composeView.setContent {
      FilterRow(filter, singleSelectionEnabled) {
        if (singleSelectionEnabled && !filter.isSelected) { handleSingleSelection() }
        filter.isSelected = !filter.isSelected
        parentListener.onChildSelected(filter, bindingAdapterPosition) // See ExpandedFilterAdapter for diff between binding vs absolute
        filterSelectedListener.onFilterSelected(composeView, filter, singleSelectionEnabled)
      }
    }
  }
}

@Composable
fun FilterRow(filter: FilterItem, singleSelectionEnabled: Boolean, onClick: () -> Unit) {
  AppCompatTheme {
    Row(Modifier.fillMaxWidth().height(50.dp).testTag("FilterRow"), Arrangement.SpaceBetween) {
      Text(filter.name, Modifier.padding(start = 20.dp).align(Alignment.CenterVertically), fontSize = 20.sp)
      if (singleSelectionEnabled) {
        RadioButton(filter.isSelected, onClick,
          Modifier.padding(end = 20.dp).align(Alignment.CenterVertically),
          colors = RadioButtonDefaults.colors(Color.Red, Color.Red))
      }
      else {
        Checkbox(filter.isSelected, { _ -> onClick() },
          Modifier.padding(end = 20.dp).align(Alignment.CenterVertically),
          colors = CheckboxDefaults.colors(Color.Red, Color.Red, Color.Yellow))
      }
    }
  }
}
@Preview(widthDp = 350, showBackground = true)
@Composable
fun FilterRowPreview() {
  FilterRow(FilterItem("Filter Name", false, "Filter Group"), false) { }
}

fun interface OnFilterSelectedListener {
    fun onFilterSelected(view : View, selectedFilter : FilterItem, singleSelectionEnabled: Boolean)
}

class FilterDiffCallback : DiffUtil.ItemCallback<FilterItem>() {
  override fun areItemsTheSame(oldFilter: FilterItem, newFilter: FilterItem): Boolean =
    oldFilter.name == newFilter.name

  // If above returns true, below will be the final check
  override fun areContentsTheSame(oldFilter: FilterItem, newFilter: FilterItem): Boolean =
    oldFilter.filterGroupName == newFilter.filterGroupName && oldFilter.isSelected == newFilter.isSelected
}