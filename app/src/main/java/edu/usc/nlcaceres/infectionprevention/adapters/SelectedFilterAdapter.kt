package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.databinding.ItemSelectedFilterBinding

/* This Adapter fills the RecyclerView that holds the filters that have actually been selected
* (whereas the other two filter adapters are used in the selection process for filters) */
class SelectedFilterAdapter(private val removeButtonListener : RemoveFilterListener) :
  ListAdapter<FilterItem, SelectedFilterAdapter.SelectedFilterViewHolder>(FilterDiffCallback()) {
  /* NOTE: ListAdapter NEEDS a new list EVERY time the data changes (whether its a addition/removal or data itself modified)
  * MAYBE a remove button is bad here OR maybe a SortedList is better here! Probably still fast with 10-100 filters BUT worth considering */

  class SelectedFilterViewHolder(private val viewBinding : ItemSelectedFilterBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(filter: FilterItem, listener : RemoveFilterListener) {
      viewBinding.selectedFilterTextView.text = filter.name
      viewBinding.removeFilterButton.setOnClickListener { button ->
        listener.onRemoveButtonClicked(button, filter, bindingAdapterPosition)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFilterViewHolder = SelectedFilterViewHolder(
    ItemSelectedFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: SelectedFilterViewHolder, position: Int) {
    holder.bind(getItem(position), removeButtonListener)
  }

}

// Adding 'fun' to interfaces w/ only 1 method inside, allows SAM conversion (aka kotlin lambdas) + trailing closures (a la Swift)
fun interface RemoveFilterListener {
  fun onRemoveButtonClicked(view : View, filter : FilterItem, position : Int)
}
