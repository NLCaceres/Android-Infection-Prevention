package edu.usc.nlcaceres.infectionprevention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.helpers.FilterItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_selected_filter.*

class AdapterSelectedFilterRV(private val selectedFilterList : ArrayList<FilterItem>, private val removeButtonListener : RemoveFilterListener) :
    RecyclerView.Adapter<AdapterSelectedFilterRV.SelectedFilterView>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFilterView = SelectedFilterView(LayoutInflater.
      from(parent.context).inflate(R.layout.item_selected_filter, parent, false))

  override fun onBindViewHolder(holder: SelectedFilterView, position: Int) {
    holder.bind(selectedFilterList[position], removeButtonListener)
  }

  override fun getItemCount(): Int = selectedFilterList.size

  class SelectedFilterView(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(filter : FilterItem, listener : RemoveFilterListener) {
      selectedFilterTextView.text = filter.name
      removeFilterButton.setOnClickListener { listener.onRemoveButtonClicked(it, filter, adapterPosition)}
    }
  }

  fun removeFilter() {

  }

  interface RemoveFilterListener {
    fun onRemoveButtonClicked(view : View, filter : FilterItem, position : Int)
  }
}