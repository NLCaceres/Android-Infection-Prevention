package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.mockito.quality.Strictness

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ViewModelSortFilterTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  @get:Rule
  var executorRule = InstantTaskExecutorRule()

  private lateinit var viewModel: ViewModelSortFilter
  private lateinit var sortGroup: FilterGroup // Useful for making new filterGroupList
  private lateinit var filterGroupList: List<FilterGroup> // Useful for comparison and searching

  @Before
  fun `Fill FilterGroups with Options`() {
    viewModel = ViewModelSortFilter(mainDispatcherRule.testDispatcher)
    assertEquals(emptyList<FilterGroup>(), viewModel.filterGroupList.value)

    val sortByTitleStr = "Sort By"
    val sortByList = arrayListOf(FilterItem("New Reports", false, sortByTitleStr),
      FilterItem("Older Reports", false, sortByTitleStr),
      FilterItem("Employee Name (A-Z)", false, sortByTitleStr),
      FilterItem("Employee Name (Z-A)", false, sortByTitleStr))

    val precautionName = "Precaution Type"
    val precautionItemList = arrayListOf(FilterItem("Standard", false, precautionName),
      FilterItem("Isolation", false, precautionName))

    val practiceName = "Health Practice Type"
    val healthPracticeItemList = arrayListOf(FilterItem("Hand Hygiene", false, practiceName),
      FilterItem("PPE", false, practiceName), FilterItem("Droplet", false, practiceName))

    sortGroup = FilterGroup(sortByTitleStr, sortByList, false, true)
    filterGroupList = listOf(sortGroup,
      FilterGroup(precautionName, precautionItemList, false, false),
      FilterGroup(practiceName, healthPracticeItemList, false, false)
    )
    // MUST collect stateflows for date population to occur
  }

  @Test fun `Check Filter Grouping and Filter Items Made Correctly`() = runTest {
    // Launch/Collecting stateflows in the before rule causes tests to hang due to runTest+launch so MUST start+cancel in each test
    val filterStateFlowJob = launch(mainDispatcherRule.testDispatcher) { viewModel.filterGroupList.collect {} }

    val precautionList = arrayListOf("Standard", "Isolation")
    val healthPracticeList = arrayListOf("Hand Hygiene", "PPE", "Droplet")
    viewModel.initializeFilterList(precautionList, healthPracticeList)
    // initialize() can be called twice (The "before" rule) BUT no doubling occurs. Just a brand new version of the same list
    assertEquals(filterGroupList, viewModel.filterGroupList.value)

    val expectedPrecautionList = FilterGroup("Precaution Type", arrayListOf(FilterItem("Standard", false, "Precaution Type"),
      FilterItem("Isolation", false, "Precaution Type")), false, false)
    val expectedFiltersWithPrecautionList = listOf(sortGroup, expectedPrecautionList) // Different input can yield a smaller list!
    viewModel.initializeFilterList(precautionList, emptyList()) // WHEN 1 empty list used
    assertEquals(2, viewModel.filterGroupList.value.size) // Only the 1 non-empty list is added
    assertEquals(expectedFiltersWithPrecautionList, viewModel.filterGroupList.value)

    val expectedHealthPracticeList = FilterGroup("Health Practice Type",
      arrayListOf(FilterItem("Hand Hygiene", false, "Health Practice Type"), FilterItem("PPE", false, "Health Practice Type"),
      FilterItem("Droplet", false, "Health Practice Type")), false, false)
    val expectedFiltersWithHealthPracticeList = listOf(sortGroup, expectedHealthPracticeList)
    viewModel.initializeFilterList(emptyList(), healthPracticeList) // Similarly but using the healthPracticeList
    assertEquals(2, viewModel.filterGroupList.value.size) // Once again, only 1 non-empty list added
    assertEquals(expectedFiltersWithHealthPracticeList, viewModel.filterGroupList.value)

    val expectedEmptyList = listOf(sortGroup) // And as small as a single list of options
    viewModel.initializeFilterList(emptyList(), emptyList()) // WHEN empty lists used
    assertEquals(1, viewModel.filterGroupList.value.size) // ONLY the default sort list in the group list
    assertEquals(expectedEmptyList, viewModel.filterGroupList.value) // Those options' lists not added

    filterStateFlowJob.cancel() // MUST ALSO CANCEL the stateflow!
  }

  @Test fun `Select New Filter Choice`() = runTest {
    val filterStateFlowJob = launch(mainDispatcherRule.testDispatcher) { viewModel.filterGroupList.collect {} }
    val precautionList = arrayListOf("Standard", "Isolation")
    val healthPracticeList = arrayListOf("Hand Hygiene", "PPE", "Droplet")
    viewModel.initializeFilterList(precautionList, healthPracticeList)

    val doneButtonStateObserver: Observer<Boolean> = mock() // Need to observe a liveData made from distinctUntilChanged()
    viewModel.doneButtonEnabled.observeForever(doneButtonStateObserver) // OR its value property will always be null

    assertEquals(0, viewModel.selectedFilterList.value?.size) // Should be empty
    assertEquals(false, viewModel.doneButtonEnabled.value) // Empty == done button disabled

    val newReportsSorter = sortGroup.filters[0] // New Reports Sorter, SingleSelect
    newReportsSorter.isSelected = true // Mark it like the adapter would
    val (removedIndex, addedIndex) = viewModel.selectFilter(newReportsSorter, true)
    assert(removedIndex == -1 && addedIndex == 0) // [] Remove nothing. Add to end so [0]
    assertEquals(1, viewModel.selectedFilterList.value?.size)
    assertEquals(newReportsSorter, viewModel.selectedFilterList.value?.last())
    assertEquals(true, viewModel.doneButtonEnabled.value) // Not empty == done button enabled!

    val oldReportsSorter = sortGroup.filters[1] // Older Reports Sorter, SingleSelect
    oldReportsSorter.isSelected = true
    val (removedIndex2, addedIndex2) = viewModel.selectFilter(oldReportsSorter, true)
    assert(removedIndex2 == 0 && addedIndex2 == 0) // [0] Remove newSorter. Add oldSorter so still [0]
    assertEquals(1, viewModel.selectedFilterList.value?.size)
    assertEquals(oldReportsSorter, viewModel.selectedFilterList.value?.last())
    // FilterAdapter would handle the underlying state of the filterGroup UI. The UI bridges the selectedFilterAdapter's state above
    assertEquals(true, newReportsSorter.isSelected) // There4 in prod, this would update BUT not here
    assertEquals(true, viewModel.doneButtonEnabled.value) // Not empty == done button enabled!

    val standardFilter = filterGroupList[1].filters[0] // Standard Precaution, Multiselect
    standardFilter.isSelected = true // Mark it like the adapter would
    val (removedIndex3, addedIndex3) = viewModel.selectFilter(standardFilter, false)
    assert(removedIndex3 == -1 && addedIndex3 == 1) // [0] Remove nothing. Add to end so now [0,1]
    assertEquals(2, viewModel.selectedFilterList.value?.size)
    assertEquals(standardFilter, viewModel.selectedFilterList.value?.last())
    assertEquals(true, viewModel.doneButtonEnabled.value) // Not empty == done button enabled!

    standardFilter.isSelected = false // Unmark it like the adapter would
    val (removedIndex4, addedIndex4) = viewModel.selectFilter(standardFilter, false)
    assert(removedIndex4 == 1 && addedIndex4 == -1) // [0,1] -> Remove index 1. Don't add anything
    assertEquals(1, viewModel.selectedFilterList.value?.size)
    assertEquals(oldReportsSorter, viewModel.selectedFilterList.value?.last()) // Now only left with old reports sorter
    assertEquals(true, viewModel.doneButtonEnabled.value) // Not empty == done button enabled!

    oldReportsSorter.isSelected = false // Unmark the singleselection one
    val (removedIndex5, addedIndex5) = viewModel.selectFilter(oldReportsSorter, true)
    assert(removedIndex5 == 0 && addedIndex5 == -1) // [0] Remove last remaining. Don't add anything
    assertEquals(0, viewModel.selectedFilterList.value?.size)
    assertEquals(false, viewModel.doneButtonEnabled.value) // Empty == done button disabled

    viewModel.doneButtonEnabled.removeObserver(doneButtonStateObserver)
    filterStateFlowJob.cancel()
  }

  @Test fun `Find and Unselect Filter Item`() = runTest {
    val filterStateFlowJob = launch(mainDispatcherRule.testDispatcher) { viewModel.filterGroupList.collect {} }
    val precautionList = arrayListOf("Standard", "Isolation")
    val healthPracticeList = arrayListOf("Hand Hygiene", "PPE", "Droplet")
    viewModel.initializeFilterList(precautionList, healthPracticeList)

    val unknownFilter = FilterItem("Unknown", false, "Unknown Group")
    assertEquals(null, viewModel.findAndUnselectFilter(unknownFilter))
    val unknownFilterKnownGroup = FilterItem("Unknown", false, "Sort By")
    assertEquals(null, viewModel.findAndUnselectFilter(unknownFilterKnownGroup))
    val knownFilterUnknownGroup = FilterItem("New Reports", false, "Unknown Group")
    assertEquals(null, viewModel.findAndUnselectFilter(knownFilterUnknownGroup))

    val newReportsSorter = sortGroup.filters[0] // New Reports Sorter, SingleSelect
    newReportsSorter.isSelected = true // Mark it like the adapter would
    assertEquals(Pair(0,0), viewModel.findAndUnselectFilter(newReportsSorter))
    assertEquals(false, newReportsSorter.isSelected) // Gets unselected if found
    filterStateFlowJob.cancel()
  }

  @Test fun `Remove Selected Filter`() = runTest {
    val filterStateFlowJob = launch(mainDispatcherRule.testDispatcher) { viewModel.filterGroupList.collect {} }
    val precautionList = arrayListOf("Standard", "Isolation")
    val healthPracticeList = arrayListOf("Hand Hygiene", "PPE", "Droplet")
    viewModel.initializeFilterList(precautionList, healthPracticeList)

    val doneButtonStateObserver: Observer<Boolean> = mock() // Need to observe a liveData made from distinctUntilChanged()
    viewModel.doneButtonEnabled.observeForever(doneButtonStateObserver) // OR its value property will always be null

    assertEquals(0, viewModel.selectedFilterList.value?.size) // Should be empty
    assertEquals(false, viewModel.doneButtonEnabled.value) // Empty == done button disabled

    val newReportsSorter = sortGroup.filters[0]
    newReportsSorter.isSelected = true // Mark like the adapter
    viewModel.selectFilter(newReportsSorter, true)
    assertEquals(1, viewModel.selectedFilterList.value?.size) // Should now have one!
    assertEquals(true, viewModel.doneButtonEnabled.value) // Not empty == done button enabled!

    viewModel.removeSelectedFilter(0) // Should only have
    assertEquals(0, viewModel.selectedFilterList.value?.size)
    assertEquals(false, viewModel.doneButtonEnabled.value) // Empty == done button disabled

    val standardFilter = filterGroupList[1].filters[0].apply { isSelected = true } // Standard Precaution, Multiselect
    val handHygieneFilter = filterGroupList[2].filters[0].apply { isSelected = true } // Hand Hygiene Practice, Multiselect
    viewModel.selectFilter(standardFilter, false)
    viewModel.selectFilter(handHygieneFilter, false)
    assertEquals(2, viewModel.selectedFilterList.value?.size)
    assertEquals(true, viewModel.doneButtonEnabled.value)

    viewModel.removeSelectedFilter(0) // Remove standard precaution filter
    assertEquals(1, viewModel.selectedFilterList.value?.size)
    assertEquals(handHygieneFilter, viewModel.selectedFilterList.value?.last()) // Only hand hygiene filter left

    viewModel.doneButtonEnabled.removeObserver(doneButtonStateObserver)
    filterStateFlowJob.cancel()
  }
  @Test fun `Reset Selected Filter`() = runTest {
    val filterStateFlowJob = launch(mainDispatcherRule.testDispatcher) { viewModel.filterGroupList.collect {} }
    val precautionList = arrayListOf("Standard", "Isolation")
    val healthPracticeList = arrayListOf("Hand Hygiene", "PPE", "Droplet")
    viewModel.initializeFilterList(precautionList, healthPracticeList)

    val doneButtonStateObserver: Observer<Boolean> = mock() // Need to observe a liveData made from distinctUntilChanged()
    viewModel.doneButtonEnabled.observeForever(doneButtonStateObserver) // OR its value property will always be null

    // Must use viewModel version of list to be certain resetFilters() flips all filters back to isSelected = false
    val precautionGroup = viewModel.filterGroupList.value[1].apply { isExpanded = true } // Make it selectable like in UI
    val standardFilter = precautionGroup.filters[0].apply { isSelected = true } // Standard Precaution, Multiselect
    viewModel.selectFilter(standardFilter, false)

    val practiceGroup = viewModel.filterGroupList.value[2].apply { isExpanded = true } // Make it selectable
    val handHygieneFilter = practiceGroup.filters[0].apply { isSelected = true } // Hand Hygiene Practice, Multiselect
    viewModel.selectFilter(handHygieneFilter, false)

    assertEquals(2, viewModel.selectedFilterList.value?.size)
    assertEquals(true, viewModel.doneButtonEnabled.value)

    val changedIndices = viewModel.resetFilters()
    assertEquals(2, changedIndices.size) // PrecautionGroup index and PracticeGroup index
    assertEquals(1, changedIndices[0]) // PrecautionGroup index == 1 in filterGroupList
    assertEquals(2, changedIndices[1]) // PracticeGroup index == 2 in filterGroupList

    assertEquals(0, viewModel.selectedFilterList.value?.size) // SelectedFilters back to 0

    viewModel.filterGroupList.value.forEach { filterGroup -> // All must have isSelected & isExpanded == false now
      filterGroup.filters.forEach { filter -> assertEquals(false, filter.isSelected) }
      assertEquals(false, filterGroup.isExpanded)
    }
    assertEquals(false, standardFilter.isSelected) // Since the 2 filters are just refs, they should be flipped too!
    assertEquals(false, handHygieneFilter.isSelected)
    assertEquals(false, viewModel.doneButtonEnabled.value) // doneButton disabled now that empty list

    viewModel.doneButtonEnabled.removeObserver(doneButtonStateObserver)
    filterStateFlowJob.cancel()
  }
}