package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.databinding.ItemSelectedFilterBinding

/* RecyclerView Adapter that fills as options are selected from a different RecyclerView
* Provides a remove button listener to help match state between this RV and the options RV's datasets
* Linked to: ExpandableFilterAdapter and each of its FilterAdapter groups */
class SelectedFilterAdapter(private val removeButtonListener : RemoveFilterListener) :
  ListAdapter<FilterItem, SelectedFilterAdapter.SelectedFilterViewHolder>(FilterDiffCallback()) {
  /* NOTE: ListAdapter NEEDS a new list instance EVERY time the data changes, whether it's an addition, removal or update
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
