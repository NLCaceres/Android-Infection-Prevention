package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import edu.usc.nlcaceres.infectionprevention.databinding.ActivitySortFilterBinding
import edu.usc.nlcaceres.infectionprevention.data.FilterGroup
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter.ExpandableFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.OnFilterSelectedListener
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.HealthPracticeListExtra
import edu.usc.nlcaceres.infectionprevention.util.PrecautionListExtra
import edu.usc.nlcaceres.infectionprevention.util.SelectedFilterParcel

/* Activity with 2 RecyclerViews - Top handles the selected filters, Bottom the filters to be selected
 Launches from: FragmentReportList */
class ActivitySortFilter : AppCompatActivity() {

  private lateinit var viewBinding : ActivitySortFilterBinding
  private var doneButtonEnabled : Boolean = false // In toolbar, allow if > 0 filters selected
  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter
  private var selectedFilterList : ArrayList<FilterItem> = arrayListOf()
  private lateinit var expandableFilterRV : RecyclerView
  private lateinit var expandableFilterAdapter : ExpandableFilterAdapter
  private var filterGroupList = arrayListOf<FilterGroup>()
  private var precautionTypeList = arrayListOf<FilterItem>()
  private var practiceTypeList = arrayListOf<FilterItem>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivitySortFilterBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_close)

    createGroupAndLists() // Before the selectedListRV (since it needs stuff to select!)

    setUpSelectedFilterRV()

    // ExpandableListView = alt choice BUT RecyclerViews CAN save on memory
    expandableFilterAdapter = ExpandableFilterAdapter(FilterSelectionListener())
    expandableFilterRV = viewBinding.expandableFilterRecyclerView.apply {
      adapter = expandableFilterAdapter
      (adapter as ExpandableFilterAdapter).submitList(filterGroupList)
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        .apply { setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_item_divider)!!) })
      //isNestedScrollingEnabled = false // Making false usually not good w/ recyclerViews - messes with recycling
    }
  }

  // Next 3 setup options menu in toolbar
  override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.sorting_actions, menu); return true }
  override fun onPrepareOptionsMenu(menu: Menu): Boolean { // Called after onCreate and in lifecycle when invalidateOptionsMenu is called!
    menu[1].apply { // Get() doneButton
      isEnabled = doneButtonEnabled // Use global var, change it, then call invalidateOptionsMenu
      icon?.alpha = if (isEnabled) 255 else 130 // Full brightness when enabled. Just over half when disabled
    }
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.set_filters_action -> {
      setResult(Activity.RESULT_OK, Intent().putParcelableArrayListExtra(SelectedFilterParcel, selectedFilterList))
      finish()
      true
    }
    R.id.reset_filters_action -> {
      val selectedFilterSize = selectedFilterList.size
      selectedFilterList.clear() // Using rangeRemoved over dataSetChange means no total recyclerview reset
      selectedFilterAdapter.notifyItemRangeRemoved(0, selectedFilterSize)

      filterGroupList.forEachIndexed { i, filterGroup -> // Possibly better than "for (i,filterGroup) in withIndex"
        filterGroup.filters.forEach { if (it.isSelected) it.isSelected = false }
        if (filterGroup.isExpanded) { filterGroup.isExpanded = false; expandableFilterAdapter.notifyItemChanged(i) }
      }

      doneButtonEnabled = false; invalidateOptionsMenu() // Invalidating calls onPrepareOptionsMenu to disable doneButton
      true
    }
    R.id.settings_action -> {
      startActivity(Intent(this, ActivitySettings::class.java))
      true
    }
    else -> { super.onOptionsItemSelected(item) }
  }

  // Following override prevents odd Hilt EntryPoint exception crash in instrumentedTests
  // Close button causes test app to restart after test completes stopping rest of test suite
  override fun onSupportNavigateUp(): Boolean { finish(); return true } // Despite override, works exactly the same as before

  private fun createGroupAndLists() {
    val sortByTitleStr = "Sort By"
    val sortByList = arrayListOf(FilterItem("New Reports", false, sortByTitleStr),
      FilterItem("Older Reports", false, sortByTitleStr),
      FilterItem("Employee Name (A-Z)", false, sortByTitleStr),
      FilterItem("Employee Name (Z-A)", false, sortByTitleStr))

    val tempPrecautionTypeList = intent.getStringArrayListExtra(PrecautionListExtra)
    val tempPracticeTypeList = intent.getStringArrayListExtra(HealthPracticeListExtra)
    val precautionName = "Precaution Type"
    val practiceName = "Health Practice Type"
    if (tempPracticeTypeList != null && tempPrecautionTypeList != null) {
      precautionTypeList = tempPracticeTypeList.map { precaution -> FilterItem(precaution, false, precautionName) } as ArrayList<FilterItem>
      precautionTypeList = tempPrecautionTypeList.map { precaution -> FilterItem(precaution, false, precautionName) } as ArrayList<FilterItem>
      practiceTypeList = tempPracticeTypeList.map { practice -> FilterItem(practice, false, practiceName) } as ArrayList<FilterItem>
    }
    // Booleans need param names hence 'isExpanded =' & 'singleSelectionEnabled'
    filterGroupList.add(FilterGroup(sortByTitleStr, sortByList, isExpanded = false, singleSelectionEnabled = true))
    filterGroupList.add(FilterGroup(precautionName, precautionTypeList, isExpanded = false, singleSelectionEnabled = false))
    filterGroupList.add(FilterGroup(practiceName, practiceTypeList, isExpanded = false, singleSelectionEnabled = false))
  }

  private fun setUpSelectedFilterRV() {
    selectedFilterAdapter = SelectedFilterAdapter removeSelectedFilter@{ _, filter, position -> // View, FilterItem, Int
      selectedFilterList.removeAt(position) // First remove filter from selectedFilterList
      selectedFilterAdapter.notifyItemRemoved(position) // Bit more efficient than submitting whole new list to diff
      // Now uncheck the filter in the expandableLists
      val filterGroupIndex = filterGroupList.indexOfFirst { it.name == filter.filterGroupName } // SHOULD never return -1 but just in case!
      if (filterGroupIndex == -1) { return@removeSelectedFilter } // Prevent indexOutOfRange exception by returning from closure (not setupFunc)
      val filterIndex = filterGroupList[filterGroupIndex].filters.indexOfFirst { it.name == filter.name }
      if (filterIndex != -1) { // Also shouldn't ever return -1 BUT just in case
        filter.isSelected = !filter.isSelected // Since it's a ref, can change checkmark here (rather than with index on the list)
        (expandableFilterRV.findViewHolderForAdapterPosition(filterGroupIndex) as ExpandableFilterViewHolder)
          .filterAdapter.notifyItemChanged(filterIndex)
      }
      if (selectedFilterList.size == 0) { doneButtonEnabled = false; invalidateOptionsMenu() } // Disable doneButton if no selected filters
    }

    selectedFilterRV = viewBinding.selectedFiltersRecyclerView.apply {
      adapter = selectedFilterAdapter
      (adapter as SelectedFilterAdapter).submitList(selectedFilterList) // Initial list submit (should be null up until here)
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
  }

  private inner class FilterSelectionListener : OnFilterSelectedListener {
    override fun onFilterSelected(view: View, selectedFilter: FilterItem, singleSelectionEnabled : Boolean) {
      // When clicking new filter, must add to selectedRV
      // If just checkmarked a filter AND it's "singleSelection only" then remove any previous one
      // If not single selection BUT just checkmarked, -1 prevents unnecessary removal. Only remove an unchecked filter
      val removalIndex = if (selectedFilter.isSelected && singleSelectionEnabled)
        selectedFilterList.indexOfFirst { it.filterGroupName == selectedFilter.filterGroupName }
        else if (selectedFilter.isSelected) { -1 }
        else selectedFilterList.indexOf(selectedFilter)
      if (removalIndex != -1) { // -1 means no match exists, therefore only remove if actually already in selectedList
        selectedFilterList.removeAt(removalIndex)
        selectedFilterAdapter.notifyItemRemoved(removalIndex)
      }
      if (selectedFilter.isSelected) {
        selectedFilterList.add(selectedFilter) // Old filter was removed, Add new one now
        selectedFilterAdapter.notifyItemInserted(selectedFilterList.size - 1)
      }
      doneButtonEnabled = selectedFilterList.isNotEmpty() // selectedFilterList.size > 0, then should be finishable!
      invalidateOptionsMenu() // Update doneButton to be enabled/tappable if selectedFilterList.size > 0
    }
  }
}