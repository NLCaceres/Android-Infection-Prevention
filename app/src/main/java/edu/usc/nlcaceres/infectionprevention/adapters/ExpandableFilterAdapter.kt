package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.databinding.ItemExpandableFilterBinding

/* RecyclerViewAdapter that contains a list of GROUPS of different filter types. Each items opens accordion-style
* containing a RecyclerView full of specific filters from that group */
class ExpandableFilterAdapter(private val filterSelectedListener: OnFilterSelectedListener) :
  ListAdapter<FilterGroup, ExpandableFilterAdapter.ExpandableFilterViewHolder>(ExpandableFilterDiffCallback()) {

  /* Not private so ActivitySortFilter can grab viewHolder, cast it and notify a change occurred (onSelection)
  * Inner class used here to allow access to Adapter members (notifyItemChanged). Note: Inner means ExFilterViewHolder gets
  * a ref to the parent aka the Adapter SO best to think about memory usage */
  inner class ExpandableFilterViewHolder(val viewBinding : ItemExpandableFilterBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    lateinit var filterAdapter : FilterAdapter
    fun bind(filterGroup : FilterGroup) {
      viewBinding.filterGroupNameTextView.text = filterGroup.name
      viewBinding.filterGroupNameTextView.setOnClickListener {
        filterGroup.isExpanded = !filterGroup.isExpanded
        notifyItemChanged(bindingAdapterPosition, "Expanded-Changed")
        /* bindingAdapterPosition = This adapter's view on the items position. absoluteAdapterPosition here would
        * probably return the same int BUT if using ConcatAdapter (vs ListAdapter), absoluteAdapterPos CAN return
        * a very different number! there4 best to use bindingAdapterPos */
      }
      filterAdapter = FilterAdapter(filterGroup.singleSelectionEnabled, filterSelectedListener)
      viewBinding.filterRecyclerView.apply {
        adapter = filterAdapter
        (adapter as FilterAdapter).submitList(filterGroup.filters)
        visibility = if (filterGroup.isExpanded) View.VISIBLE else View.GONE
      }
    }
    fun notifyFilterRemoved(position : Int) {
      filterAdapter.notifyItemChanged(position)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableFilterViewHolder = ExpandableFilterViewHolder(
    ItemExpandableFilterBinding.inflate(LayoutInflater.from(parent.context), parent,false))

  override fun onBindViewHolder(holder: ExpandableFilterViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onBindViewHolder(holder: ExpandableFilterViewHolder, position: Int, payloads: MutableList<Any>) {
    if (payloads.isNotEmpty()) {
      val expandPayload = payloads.firstOrNull { payload -> (payload as String) == "Expanded-Changed" } as? String
      if (expandPayload != null) { // If not null, we SHOULD have "Expanded-Changed"
          val filterGroup = getItem(position)
          holder.viewBinding.filterRecyclerView.visibility = if (filterGroup.isExpanded) View.VISIBLE else View.GONE
      }
      return // Ensure any code below running
    }
    onBindViewHolder(holder, position) // Ensure this fires in case payloads are indeed empty
  }
}

private class ExpandableFilterDiffCallback : DiffUtil.ItemCallback<FilterGroup>() {
  override fun areItemsTheSame(oldFilterGroup: FilterGroup, newFilterGroup: FilterGroup): Boolean =
    oldFilterGroup.name == newFilterGroup.name

  override fun areContentsTheSame(oldFilterGroup: FilterGroup, newFilterGroup: FilterGroup): Boolean =
    oldFilterGroup.filters.size == newFilterGroup.filters.size
}