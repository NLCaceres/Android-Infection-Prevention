package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.databinding.ActivitySortFilterBinding
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter.ExpandableFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.OnFilterSelectedListener
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.HealthPracticeListExtra
import edu.usc.nlcaceres.infectionprevention.util.PrecautionListExtra
import edu.usc.nlcaceres.infectionprevention.util.SelectedFilterParcel
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelSortFilter
import kotlinx.coroutines.launch

/* Activity with 2 RecyclerViews - Top handles the selected filters, Bottom the filters to be selected
 Launches from: FragmentReportList */
@AndroidEntryPoint
class ActivitySortFilter : AppCompatActivity() {

  private lateinit var viewBinding : ActivitySortFilterBinding
  private val viewModel: ViewModelSortFilter by viewModels()

  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter

  private lateinit var expandableFilterRV : RecyclerView
  private lateinit var expandableFilterAdapter : ExpandableFilterAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivitySortFilterBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_close)

    // Since doneButtonEnabled is distinct(), invalidate() always needed so onPrepareOptionsMenu can update doneButton
    viewModel.doneButtonEnabled.observe(this) { invalidateOptionsMenu() }

    setUpSelectedFilterRV()

    setupExpandableRecyclerView()
  }

  // Next 3 setup options menu in toolbar
  override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.sorting_actions, menu); return true }
  override fun onPrepareOptionsMenu(menu: Menu): Boolean { // Called after onCreate and in lifecycle when invalidateOptionsMenu is called!
    menu[1].apply { // Get() doneButton
      isEnabled = viewModel.doneButtonEnabled.value!! // Since invalidate() called from observe, value should be ready to use
      icon?.alpha = if (isEnabled) 255 else 130 // Full brightness when enabled. Just over half when disabled
    }
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.set_filters_action -> {
      val selectedFilters = ArrayList(viewModel.selectedFilterList.value ?: listOf())
      setResult(Activity.RESULT_OK, Intent().putParcelableArrayListExtra(SelectedFilterParcel, selectedFilters))
      finish()
      true
    }
    R.id.reset_filters_action -> { // Reset selectedFilters & use returned indices to update specific items' state
      viewModel.resetFilters().forEach { index -> expandableFilterAdapter.notifyItemChanged(index) }
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

  private fun setupExpandableRecyclerView() {
    // ExpandableListView = alt choice BUT RecyclerViews CAN save on memory
    expandableFilterRV = viewBinding.expandableFilterRecyclerView.apply {
      expandableFilterAdapter = ExpandableFilterAdapter(FilterSelectionListener()).also { adapter = it }
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        .apply { setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_item_divider)!!) })
      //isNestedScrollingEnabled = false // Making false usually not good w/ recyclerViews - messes with recycling
    }
    // Unlike liveData, stateflow must collect in launch & use flowWithLifecycle to avoid collecting when view paused/stopped
    lifecycleScope.launch { viewModel.filterGroupList.flowWithLifecycle(lifecycle).collect { newList ->
      expandableFilterAdapter.submitList(newList)
    }}
    val precautionTypeList = intent.getStringArrayListExtra(PrecautionListExtra) ?: emptyList()
    val practiceTypeList = intent.getStringArrayListExtra(HealthPracticeListExtra) ?: emptyList()
    viewModel.initializeFilterList(precautionTypeList, practiceTypeList)
  }

  private fun setUpSelectedFilterRV() {
    selectedFilterAdapter = SelectedFilterAdapter removeSelectedFilter@{ _, filter, position -> // View, FilterItem, Int
      viewModel.removeSelectedFilter(position) // First remove filter from selectedFilterList
      selectedFilterAdapter.notifyItemRemoved(position) // Bit more efficient than submitting whole new list to diff

      // Now find the indices to use in the filterAdapter to uncheck the filter
      viewModel.findAndUnselectFilter(filter)?.let { (filterGroupIndex, filterIndex) ->
        (expandableFilterRV.findViewHolderForAdapterPosition(filterGroupIndex) as ExpandableFilterViewHolder)
          .filterAdapter.notifyItemChanged(filterIndex)
      }
    }

    selectedFilterRV = viewBinding.selectedFiltersRecyclerView.apply {
      adapter = selectedFilterAdapter
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
    viewModel.selectedFilterList.observe(this) { newList -> selectedFilterAdapter.submitList(newList) }
  }

  private inner class FilterSelectionListener : OnFilterSelectedListener {
    override fun onFilterSelected(view: View, selectedFilter: FilterItem, singleSelectionEnabled : Boolean) {
      // Unmark any previous radiobutton selected or if filter already selected. Add the newly selected filter to adapter
      val (removalIndex, addedIndex) = viewModel.selectFilter(selectedFilter, singleSelectionEnabled)
      if (removalIndex != -1) { selectedFilterAdapter.notifyItemRemoved(removalIndex) }
      if (addedIndex != -1) { selectedFilterAdapter.notifyItemInserted(addedIndex) }
    }
  }
}