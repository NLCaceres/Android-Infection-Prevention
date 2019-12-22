package edu.usc.nlcaceres.infectionprevention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.helpers.FilterItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_filter_checkbox.*

class AdapterFilter(private val filterList : ArrayList<FilterItem>, private val singleSelectionEnabled : Boolean, private val filterSelectedListener : OnFilterSelectedListener) :
    RecyclerView.Adapter<AdapterFilter.FilterView>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterView = FilterView(LayoutInflater.
  from(parent.context).inflate(R.layout.item_filter_checkbox, parent, false))

  override fun getItemCount(): Int = filterList.size

  override fun onBindViewHolder(holder: FilterView, position: Int) {
    holder.bind(filterList[position])
  }
  inner class FilterView(override val containerView : View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(filterOption : FilterItem) {
      filterCheckNameTextView.apply {
        text = filterOption.name
        isChecked = filterOption.isSelected
        setOnClickListener {
          this.isChecked = !this.isChecked
          filterOption.isSelected = this.isChecked
          handleSingleSelection(filterOption, this.isChecked)
          filterSelectedListener.onFilterSelected(this, filterOption, singleSelectionEnabled)
        }
      }
    }
  }

  fun handleSingleSelection(filterOption : FilterItem, checked : Boolean) {
    if (singleSelectionEnabled && checked) {
      for (filterItem in filterList) { if (filterItem != filterOption) filterItem.isSelected = false }
      notifyDataSetChanged()
    }
  }

  interface OnFilterSelectedListener {
    fun onFilterSelected(view : View, selectedFilter : FilterItem, singleSelectionEnabled: Boolean)
  }
}