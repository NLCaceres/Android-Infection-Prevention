package edu.usc.nlcaceres.infectionprevention.helpers.data

import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.data.FilterItem

class FiltersFactory {
  companion object Factory {
    private var createdFilterGroups = 0
    private var createdFilterItems = 0
    fun buildFilterGroup(numFilterItems : Int = 2, singleSelection : Boolean = false) : FilterGroup {
      // Simpler + more idiomatic than initing an ArrayList(size) followed by a for loop adding items
      val filterItemList = Array(numFilterItems) { buildFilterItem() }.toCollection(ArrayList())
      return FilterGroup("Group${createdFilterGroups++}", filterItemList, isExpanded = false, singleSelectionEnabled = singleSelection)
    }
    fun buildFilterItem() = FilterItem("ItemName$createdFilterItems", false, "Group${createdFilterItems++}")
  }
}