package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import edu.usc.nlcaceres.infectionprevention.adapters.OnFilterSelectedListener
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.selectedFilterParcel

/* Activity with 2 RecyclerViews - Top handles the selected filters, Bottom the filters to be selected
 Launches from: ActivityReportList */
class ActivitySortFilter : AppCompatActivity() {

  private lateinit var viewBinding : ActivitySortFilterBinding
  private var doneButtonEnabled : Boolean = false // In toolbar, allow if > 0 filters selected
  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter
  private var selectedFilterList : ArrayList<FilterItem> = arrayListOf()
  private var filterNames : ArrayList<String> = arrayListOf()
  private lateinit var expandableFilterRV : RecyclerView
  private lateinit var expandableFilterAdapter : ExpandableFilterAdapter
  private var filterGroupList = arrayListOf<FilterGroup>()
  private var precautionTypeList = arrayListOf<FilterItem>()
  private var practiceTypeList = arrayListOf<FilterItem>()

  // TODO: Consider either a complete button and/or reset button
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
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      //isNestedScrollingEnabled = false // Making false usually not good w/ recyclerViews - messes with recycling
    }
  }

  // Next 3 setup options menu in toolbar
  override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.sorting_actions, menu); return true }
  override fun onPrepareOptionsMenu(menu: Menu): Boolean { // Called after onCreate and in lifecycle when invalidateOptionsMenu is called!
    menu[1].apply { // Get() doneButton
      isEnabled = doneButtonEnabled // Use global var, change it, then call invalidateOptionsMenu
      icon.alpha = if (isEnabled) 255 else 130
    }
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.set_filters_action -> {
      setResult(Activity.RESULT_OK, Intent().putParcelableArrayListExtra(selectedFilterParcel, selectedFilterList))
      finish()
      true
    }
    R.id.reset_filters_action -> {
      selectedFilterList.clear(); selectedFilterAdapter.submitList(arrayListOf())
      for (filterGroup in filterGroupList) { filterGroup.filters.forEach { it.isSelected = false } }
      doneButtonEnabled = false; invalidateOptionsMenu() // invalidate calls onPrepareOptionsMenu which updates doneButton in toolbar
      true
    }
    R.id.settings_action -> {
      startActivity(Intent(this, ActivitySettings::class.java))
      true
    }
    else -> { super.onOptionsItemSelected(item) }
  }

  // Below uses normal back animation but otherwise same functionality (useful in fragments)
//  override fun onSupportNavigateUp(): Boolean { finish(); return true }

  private fun createGroupAndLists() {
    val sortByTitleStr = "Sort By"
    val sortByList = arrayListOf(FilterItem("Date Reported", false, sortByTitleStr),
        FilterItem("Employee Name (A-Z)", false, sortByTitleStr),
        FilterItem("Employee Name (Z-A)", false, sortByTitleStr))

    val tempPrecautionTypeList = intent.getStringArrayListExtra("PrecautionList")
    val tempPracticeTypeList = intent.getStringArrayListExtra("PracticeList")
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
    selectedFilterAdapter = SelectedFilterAdapter { _, filter, position -> // View, FilterItem, Int
      selectedFilterList.removeAt(position)//; selectedFilterAdapter.notifyItemRemoved(position)
      // Below inits new list in memory w/ references of each item in our activity's list! SO
      selectedFilterAdapter.submitList(ArrayList(selectedFilterList)) // SubmitList'll properly diff & not ignore removal!
      for ((index, filterGroup) in filterGroupList.withIndex()) {
        val checkedFilterPosition = filterGroup.filters.indexOf(filter)
        if (checkedFilterPosition == -1) continue // Prevent running following code at indexes it shouldn't
        else {
          filter.isSelected = !filter.isSelected // Since it's a ref, no need to change set at list position
          (expandableFilterRV.findViewHolderForAdapterPosition(index) as ExpandableFilterAdapter.ExpandableFilterViewHolder).
            filterAdapter.notifyItemChanged(checkedFilterPosition) // TODO Follow selectedFilterList removal example via submitList
          if (selectedFilterList.size == 0) { // Now at 0 so invalidate & update menu!
            doneButtonEnabled = false; invalidateOptionsMenu()
          }
          break // No more looping (no duplicates in list)
        }
      }
    }
    selectedFilterRV = viewBinding.selectedFiltersRecyclerView.apply {
      adapter = selectedFilterAdapter
      (adapter as SelectedFilterAdapter).submitList(selectedFilterList) // Initial list submit (should be null up until here)
      visibility = if (selectedFilterList.size > 0) View.VISIBLE else View.GONE
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
  }

  private inner class FilterSelectionListener : OnFilterSelectedListener {
    override fun onFilterSelected(view: View, selectedFilter: FilterItem, singleSelectionEnabled : Boolean) {
      if (selectedFilter.isSelected) { // Selected (checked) so update new insertion at end
        if (singleSelectionEnabled) { // If singleSelection type, must remove old selection first!
          for ((index, filterItem) in selectedFilterList.withIndex()) {
            if (filterItem.filterGroupName == selectedFilter.filterGroupName) {
              selectedFilterList.remove(filterItem)
              selectedFilterRV.adapter?.notifyItemRemoved(index)
              break
            }
          }
        }
        selectedFilterList.add(selectedFilter) // Add now that old has been removed
        selectedFilterRV.adapter?.notifyItemInserted(selectedFilterList.size - 1)
      } else { // If selection == false (unchecked) then remove and update adapter properly
        val removedIndex = selectedFilterList.indexOf(selectedFilter)
        selectedFilterList.remove(selectedFilter)
        selectedFilterRV.adapter?.notifyItemRemoved(removedIndex)
      }
      if (selectedFilterList.size > 0) {
        selectedFilterRV.visibility = View.VISIBLE
        doneButtonEnabled = true
        invalidateOptionsMenu()
      } else {
        selectedFilterRV.visibility = View.GONE
        doneButtonEnabled = false
        invalidateOptionsMenu()
      }
    }
  }
}