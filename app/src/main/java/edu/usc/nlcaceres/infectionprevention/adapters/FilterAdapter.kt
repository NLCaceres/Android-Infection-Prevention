package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.databinding.ItemFilterCheckboxBinding

/* RecyclerView Adapter used to select different types of filters each contained in an expandable/accordion view
* that is multichoice by default (typical checkbox behavior) and single choice if enabled.  */
class FilterAdapter(private val singleSelectionEnabled : Boolean, private val filterSelectedListener : OnFilterSelectedListener) :
  ListAdapter<FilterItem, FilterAdapter.FilterViewHolder>(FilterDiffCallback()) {

  /* Inner class needed to set up handleSingleSelection (maybe best passed into constructor?) */
  inner class FilterViewHolder(private val viewBinding : ItemFilterCheckboxBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(filter : FilterItem) {
      viewBinding.filterCheckNameTextView.apply {
        text = filter.name
        isChecked = filter.isSelected
        setOnClickListener {
          this.isChecked = !this.isChecked
          filter.isSelected = this.isChecked
          handleSingleSelection(filter, this.isChecked)
          filterSelectedListener.onFilterSelected(this, filter, singleSelectionEnabled)
        }
      }
    }
  }

  fun handleSingleSelection(filterOption : FilterItem, checked : Boolean) {
    if (singleSelectionEnabled && checked) {
      for (filterItem in currentList) { if (filterItem != filterOption) filterItem.isSelected = false }
//      notifyDataSetChanged()
    /* Shouldn't need notifyDateSetChanged() anymore BUT if it is, then a SortedList may work (it binds outside of the adapter / in view)
    SortedList takes the class type contained + a SortedList.Callback (SortedList.BatchedCallback or SortedListAdapterCallback, the latter = better) */
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder = FilterViewHolder(
    ItemFilterCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

fun interface OnFilterSelectedListener {
    fun onFilterSelected(view : View, selectedFilter : FilterItem, singleSelectionEnabled: Boolean)
}

class FilterDiffCallback : DiffUtil.ItemCallback<FilterItem>() {
  override fun areItemsTheSame(oldFilter: FilterItem, newFilter: FilterItem): Boolean =
    oldFilter.name == newFilter.name

  // If above returns true, below will be the final check
  override fun areContentsTheSame(oldFilter: FilterItem, newFilter: FilterItem): Boolean =
    oldFilter.filterGroupName == newFilter.filterGroupName
}