package edu.usc.nlcaceres.infectionprevention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.helpers.FilterGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_expandable_filter.*

class AdapterExpandableFilter(private val filterGroups : ArrayList<FilterGroup>, private val filterSelectedListener: AdapterFilter.OnFilterSelectedListener) : RecyclerView.Adapter<AdapterExpandableFilter.ExpandableFilterView>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableFilterView = ExpandableFilterView(LayoutInflater.
  from(parent.context!!).inflate(R.layout.item_expandable_filter, parent, false))

  override fun onBindViewHolder(holder: ExpandableFilterView, position: Int) {
    holder.bind(filterGroups[position])
  }

  override fun onBindViewHolder(holder: ExpandableFilterView, position: Int, payloads: MutableList<Any>) {
    if (payloads.isNotEmpty()) {
      val expandPayload = payloads.firstOrNull { payload -> (payload as String) == "Expanded-Changed" } as? String
      if (expandPayload != null) { holder.filterRecyclerView.visibility = if (filterGroups[position].isExpanded) View.VISIBLE else View.GONE }
      return
    }
    onBindViewHolder(holder, position) // Ensure this fires in case payloads are indeed empty
  }

  override fun getItemCount(): Int = filterGroups.size

  inner class ExpandableFilterView(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    lateinit var filterAdapter : AdapterFilter

    fun bind(filterOptions: FilterGroup) {
      filterGroupNameTextView.text = filterOptions.name
      filterGroupNameTextView.setOnClickListener {
         filterOptions.isExpanded = !filterOptions.isExpanded; notifyItemChanged(adapterPosition, "Expanded-Changed")
      }
      filterAdapter = AdapterFilter(filterOptions.filters, filterOptions.singleSelectionEnabled, filterSelectedListener)
      filterRecyclerView.apply {
        adapter = filterAdapter
        visibility = if (filterOptions.isExpanded) View.VISIBLE else View.GONE
      }
    }
    fun notifyFilterRemoved(position : Int) {
      filterAdapter.notifyItemChanged(position)
    }
  }
}