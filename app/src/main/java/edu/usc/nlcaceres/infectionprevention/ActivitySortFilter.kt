package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import edu.usc.nlcaceres.infectionprevention.helpers.FilterGroup
import edu.usc.nlcaceres.infectionprevention.helpers.FilterItem
import edu.usc.nlcaceres.infectionprevention.helpers.selectedFilterParcel

class ActivitySortFilter : AppCompatActivity() {

  private var filterNames : ArrayList<String> = arrayListOf()
  private var doneButtonEnabled : Boolean = false

  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : AdapterSelectedFilterRV
  private var selectedFilterList : ArrayList<FilterItem> = arrayListOf()

  private lateinit var expandableFilterRV : RecyclerView
  private lateinit var expandableFilterAdapter : AdapterExpandableFilter
  private var filterGroupList : ArrayList<FilterGroup> = arrayListOf()

  private var precautionTypeList : ArrayList<FilterItem> = arrayListOf()
  private var practiceTypeList : ArrayList<FilterItem> = arrayListOf()

  // TODO: Consider either a complete button and/or reset button
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sort_filter)

    val createReportToolbar = findViewById<Toolbar>(R.id.home_toolbar)
    setSupportActionBar(createReportToolbar)
    supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

//    filterListView = findViewById<ExpandableListView>(R.id.filterExpandableListView).apply {
//      setAdapter(FilterExpandableListAdapter(context, filterNames, expandableLVMap))
//      setOnChildClickListener(FilterSelectionListener())
//    }

    createGroupAndLists()

    setUpSelectedFilterRV()

    expandableFilterAdapter = AdapterExpandableFilter(filterGroupList, FilterSelectionListener())
    expandableFilterRV = findViewById<RecyclerView>(R.id.expandableFilterRecyclerView).apply {
      adapter = expandableFilterAdapter
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      //isNestedScrollingEnabled = false // Nested scrolling disabled generally not good for recyclerviews (messes with recycling function!)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.sorting_actions, menu); return true }
  override fun onPrepareOptionsMenu(menu: Menu?): Boolean { // Called after onCreate and in lifecycle when invalidateOptionsMenu is called!
    menu?.get(1)?.apply {
      isEnabled = doneButtonEnabled // Use global var, change it, then call invalidateOptionsMenu
      icon.alpha = if (isEnabled) 255 else 130
    }
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.set_filters_action -> { setResult(Activity.RESULT_OK, Intent().putParcelableArrayListExtra(selectedFilterParcel, selectedFilterList)); finish(); true }
    R.id.reset_filters_action -> {
      selectedFilterList.clear(); selectedFilterAdapter.notifyDataSetChanged()
      for (filterGroup in filterGroupList) { filterGroup.filters.forEach { it.isSelected = false } }
      expandableFilterAdapter.notifyDataSetChanged(); doneButtonEnabled = false; invalidateOptionsMenu()
      true
    }
    R.id.settings_action -> { startActivity(Intent(this, ActivitySettings::class.java)); true }
    else -> { super.onOptionsItemSelected(item) }
  }

  //override fun onSupportNavigateUp(): Boolean { finish(); return true } // Uses normal back animation but otherwise same functionality (useful in fragments)

  private fun createGroupAndLists() {
    val sortByName = "Sort By"
    val sortByList = arrayListOf(FilterItem("Date Reported", false, sortByName),
        FilterItem("Employee Name (A-Z)", false, sortByName),
        FilterItem("Employee Name (Z-A)", false, sortByName))

    val precautionName = "Precaution Type"
    val tempPrecautionTypeList = intent.getStringArrayListExtra("PrecautionList") ?: null
    val practiceName = "Health Practice Type"
    val tempPracticeTypeList = intent.getStringArrayListExtra("PracticeList") ?: null
    if (tempPracticeTypeList != null && tempPrecautionTypeList != null) {
      precautionTypeList = tempPracticeTypeList.map { precaution -> FilterItem(precaution, false, precautionName) } as ArrayList<FilterItem>
      precautionTypeList = tempPrecautionTypeList.map { precaution -> FilterItem(precaution, false, precautionName) } as ArrayList<FilterItem>
      practiceTypeList = tempPracticeTypeList.map { practice -> FilterItem(practice, false, practiceName) } as ArrayList<FilterItem>
    }

    val sortFilterGroup = FilterGroup(sortByName, sortByList, isExpanded = false, singleSelectionEnabled = true)
    filterGroupList.add(sortFilterGroup)
    val precautionFilterGroup = FilterGroup(precautionName, precautionTypeList, isExpanded = false, singleSelectionEnabled = false)
    filterGroupList.add(precautionFilterGroup)
    val practiceFilterGroup = FilterGroup(practiceName, practiceTypeList, isExpanded = false, singleSelectionEnabled = false)
    filterGroupList.add(practiceFilterGroup)
  }

  private fun setUpSelectedFilterRV() {
    selectedFilterAdapter = AdapterSelectedFilterRV(selectedFilterList, object : AdapterSelectedFilterRV.RemoveFilterListener {
      override fun onRemoveButtonClicked(view: View, filter : FilterItem, position: Int) {
        selectedFilterList.removeAt(position); selectedFilterAdapter.notifyItemRemoved(position)
        for ((index, filterGroup) in filterGroupList.withIndex()) {
          val checkedFilterPosition = filterGroup.filters.indexOf(filter)
          if (checkedFilterPosition == -1) continue // Prevent running following code at indexes it shouldn't
          else {
            filter.isSelected = !filter.isSelected // Since it's a ref, no need to change set at list position
            (expandableFilterRV.findViewHolderForAdapterPosition(index) as AdapterExpandableFilter.ExpandableFilterView).filterAdapter.notifyItemChanged(checkedFilterPosition)
            if (selectedFilterList.size == 0) { doneButtonEnabled = false; invalidateOptionsMenu() } // Hit zero so update menu!
            break // No more looping (no duplicates in list)
          }
        }
      }
    })
    selectedFilterRV = findViewById<RecyclerView>(R.id.selectedFiltersRecyclerView).apply {
      adapter = selectedFilterAdapter; visibility = if (selectedFilterList.size > 0) View.VISIBLE else View.GONE
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
  }

  private inner class FilterSelectionListener : AdapterFilter.OnFilterSelectedListener {
    override fun onFilterSelected(view: View, selectedFilter: FilterItem, singleSelectionEnabled : Boolean) {
      if (selectedFilter.isSelected) { // Selected (checked) so update new insertion at end
        if (singleSelectionEnabled) { // Unless singleSelection so you must remove first!
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