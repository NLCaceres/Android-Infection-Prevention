package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ViewModelSortFilter @Inject constructor(private val ioDispatcher: CoroutineDispatcher) : ViewModel() {
  private val _doneButtonEnabled = MutableLiveData(false)
  val doneButtonEnabled = _doneButtonEnabled.distinctUntilChanged()

  // Using SnapshotStateList makes observing List changes super easy compared to LiveData and StateFlow
  // SINCE they benefited from RecyclerViewAdapter having its notifyItem() methods SO SINCE
  // Composables lack those helpful methods, changes must be made to a copy of the List which can be
  // emitted once done changing. SnapshotStateList simply emits a new List after applying changes to the current List!
  val selectedFilterList = mutableStateListOf<FilterItem>()

  fun removeSelectedFilter(index: Int) {
    selectedFilterList.removeAt(index)
    _doneButtonEnabled.value = selectedFilterListNotEmpty()
  }
  fun resetFilters(): List<Int> {
    selectedFilterList.clear()

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
  private fun selectedFilterListNotEmpty() = selectedFilterList.isNotEmpty()

  // No stateIn() needed since the cold flow creating data in initFilterList() for these 2 stateFlows needs
  // to receive data from FragmentSortFilter's bundle to use as params during collection
  private val _filterGroupList = MutableStateFlow(emptyList<FilterGroup>())
  val filterGroupList: StateFlow<List<FilterGroup>> = _filterGroupList.asStateFlow()
  // COULD replace filterGroupList w/ a single LiveData var BUT given the power of Flows, this pattern is
  // VERY USEFUL to know ESPECIALLY if the Fragments's Bundle data wasn't needed SINCE stateIn() would consolidate even more
  // by running the cold flow, turning it into a StateFlow, scoped to viewModelScope, cancelling when the VM dies,
  // AND the flow would only ever run ONCE, saving its result to emit to all collectors, saving plenty of time!
  // Ex: `val filterGroupList: StateFlow<List<FilterGroup>> = flow { emit(foo) }.flowOn(ioDispatcher).stateIn(viewModelScope)

  fun initializeFilterList(precautionList: List<String>, healthPracticeList: List<String>) {
    val flow = flow { emit(emptyList()); emit(createGroupAndLists(precautionList, healthPracticeList)) }.flowOn(ioDispatcher)
    flow.onEach { _filterGroupList.value = it }.launchIn(viewModelScope) // Could use LiveData in a similar way
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
    val removalIndex = if (filter.isSelected && singleSelectionEnabled) // If radio button and selected a filter
      selectedFilterList.indexOfFirst { it.filterGroupName == filter.filterGroupName } // Find the last selected radio button to unmark it
      else if (filter.isSelected) { -1 } // If it's not single selection (a checkbox), no need to remove anything
      else selectedFilterList.indexOf(filter) // UNLESS user just tapped a marked filter to unselect it

    var lastIndex: Int = -1 // If just selected a filter, then update this var below to list's last index
    selectedFilterList.run {
      if (removalIndex != -1) { selectedFilterList.removeAt(removalIndex) }

      if (filter.isSelected) { selectedFilterList.add(filter); lastIndex = selectedFilterList.size - 1 }
    }

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