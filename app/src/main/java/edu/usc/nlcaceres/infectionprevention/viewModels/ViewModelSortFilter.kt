package edu.usc.nlcaceres.infectionprevention.viewModels

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelSortFilter @Inject constructor(private val ioDispatcher: CoroutineDispatcher) : ViewModel() {
  private val _doneButtonEnabled = MutableLiveData(false)
  val doneButtonEnabled = _doneButtonEnabled.distinctUntilChanged()

  // Using a mutableList in _selectedFilterList IS faster (~30-45ms vs ~2-4ms at n<100 & n>1000) since no conversion needed
  // BUT using a list provides better encapsulation, so the "out" keyword allows us to have the best of both worlds!
  // It lets a class that extends List to be declared. Here it'll ONLY read "out" simple immutable lists
  private val _selectedFilterList = MutableLiveData(mutableListOf<FilterItem>())
  val selectedFilterList: LiveData<out List<FilterItem>> = _selectedFilterList

  fun removeSelectedFilter(index: Int) {
    _selectedFilterList.value?.removeAt(index)
    _doneButtonEnabled.value = selectedFilterListNotEmpty()
  }
  fun resetFilters(): List<Int> {
    _selectedFilterList.value = mutableListOf() // No need to clear. Just emit a new emptylist

    val changedIndices = mutableListOf<Int>()
    // Go thru all the filters and unselect the selected ones for the full reset effect!
    filterGroupList.value.forEachIndexed { i, filterGroup ->
      filterGroup.filters.forEach { if (it.isSelected) it.isSelected = false }
      if (filterGroup.isExpanded) { filterGroup.isExpanded = false; changedIndices.add(i) }
    }

    _doneButtonEnabled.value = false // Selected filters cleared so must always disable button
    return changedIndices
  }
  // A null list == an empty list, An empty list returns false. A filled list returns true
  private fun selectedFilterListNotEmpty() = selectedFilterList.value?.isNotEmpty() ?: false

  // No stateIn() needed since the cold flow backing these 2 stateFlows needs view's bundle for its params to collect
  private val _filterGroupList = MutableStateFlow(emptyList<FilterGroup>())
  val filterGroupList: StateFlow<List<FilterGroup>> = _filterGroupList
  // Consequently very similar could be done using the same w/ a flow but instead backing an exposed liveData
  // BUT given the power of flows this pattern could be very useful to know ESPECIALLY if no view bundle was needed for params
  // Since stateIn() would let all collectors share 1 SYNC'd stateflow backed by 1 SYNC'd cold flow

  fun initializeFilterList(precautionList: List<String>, healthPracticeList: List<String>) {
    val flow = flow { emit(emptyList()); emit(createGroupAndLists(precautionList, healthPracticeList)) }.flowOn(ioDispatcher)
    viewModelScope.launch { flow.collect { _filterGroupList.value = it } } // Could use liveData in a similar way
  }

  private fun createGroupAndLists(precautionList: List<String>, healthPracticeList: List<String>): List<FilterGroup> {
    val sortByFilterGroupTitle = "Sort By"
    val sortByList = arrayListOf(FilterItem("New Reports", false, sortByFilterGroupTitle),
      FilterItem("Older Reports", false, sortByFilterGroupTitle),
      FilterItem("Employee Name (A-Z)", false, sortByFilterGroupTitle),
      FilterItem("Employee Name (Z-A)", false, sortByFilterGroupTitle))

    // Kotlin prefers to use named arguments for boolean properties hence 'isExpanded =' & 'singleSelectionEnabled'
    val filterGroupList = mutableListOf(FilterGroup(sortByFilterGroupTitle, sortByList, isExpanded = false, singleSelectionEnabled = true))

    if (precautionList.isNotEmpty()) {
      val filterGroupTitle = "Precaution Type"
      val precautionTypeList = precautionList.map { precautionName -> FilterItem(precautionName, false, filterGroupTitle) }
      filterGroupList.add(FilterGroup(filterGroupTitle, precautionTypeList, isExpanded = false, singleSelectionEnabled = false))
    }
    if (healthPracticeList.isNotEmpty()) {
      val filterGroupTitle = "Health Practice Type"
      val practiceTypeList = healthPracticeList.map { practiceName -> FilterItem(practiceName, false, filterGroupTitle) }
      filterGroupList.add(FilterGroup(filterGroupTitle, practiceTypeList, isExpanded = false, singleSelectionEnabled = false))
    }

    return filterGroupList
  }
  fun selectFilter(filter: FilterItem, singleSelectionEnabled: Boolean): Pair<Int, Int> {
    val mutableList = _selectedFilterList.value ?: mutableListOf()

    val removalIndex = if (filter.isSelected && singleSelectionEnabled) // If radio button and selected a filter
      mutableList.indexOfFirst { it.filterGroupName == filter.filterGroupName } // Find the last selected radio button to unmark it
      else if (filter.isSelected) { -1 } // If it's not single selection (a checkbox), no need to remove anything
      else mutableList.indexOf(filter) // UNLESS user just tapped a marked filter to unselect it

    if (removalIndex != -1) { mutableList.removeAt(removalIndex) }

    var lastIndex: Int = -1 // If just selected a filter, then update this var to list's last index so adapter can use
    if (filter.isSelected) { mutableList.add(filter); lastIndex = mutableList.size - 1 }

    _doneButtonEnabled.value = selectedFilterListNotEmpty() // If filters selected (size > 0), enable done button
    return Pair(removalIndex, lastIndex)
  }
  fun findAndUnselectFilter(filter: FilterItem): Pair<Int, Int>? {
    val filterGroupIndex = filterGroupList.value.indexOfFirst { it.name == filter.filterGroupName }
    // SHOULDN'T ever get -1 but good to use as a guard condition to avoid IndexOutOfRange
    if (filterGroupIndex == -1) { return null }

    val filterIndex = filterGroupList.value[filterGroupIndex].filters.indexOfFirst { it.name == filter.name }
    if (filterIndex == -1) { return null } // Also shouldn't ever get -1 BUT just in case

    filter.isSelected = !filter.isSelected // Since it's a ref, can change checkmark here (rather than with index on the list)
    return Pair(filterGroupIndex, filterIndex)
  }
}