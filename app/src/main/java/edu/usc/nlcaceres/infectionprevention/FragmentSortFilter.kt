package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter
import edu.usc.nlcaceres.infectionprevention.adapters.ExpandableFilterAdapter.ExpandableFilterViewHolder
import edu.usc.nlcaceres.infectionprevention.adapters.OnFilterSelectedListener
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentSortFilterBinding
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelSortFilter
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain
import kotlinx.coroutines.launch

/* Fragment with 2 RecyclerViews - Top tracks the selected filters from the Bottom containing filters to select
* Launches from: FragmentReportList */
@AndroidEntryPoint
class FragmentSortFilter : Fragment(R.layout.fragment_sort_filter) {

  private var _viewBinding: FragmentSortFilterBinding? = null
  private val viewBinding get() = _viewBinding!!
  private val viewModel: ViewModelSortFilter by viewModels()
  private val activityViewModel: ViewModelMain by activityViewModels()

  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter

  private lateinit var expandableFilterRV : RecyclerView
  private lateinit var expandableFilterAdapter : ExpandableFilterAdapter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentSortFilterBinding.inflate(inflater, container, false)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().addMenuProvider(SortFilterMenu(), viewLifecycleOwner, Lifecycle.State.RESUMED)

    // Since doneButtonEnabled is distinct(), invalidate() always needed so onPrepareOptionsMenu can update doneButton
    viewModel.doneButtonEnabled.observe(viewLifecycleOwner) { requireActivity().invalidateOptionsMenu() }
    setUpSelectedFilterRV()

    setupExpandableRecyclerView()
  }

  private inner class SortFilterMenu: MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      menuInflater.inflate(R.menu.sorting_actions, menu)
    }
    override fun onPrepareMenu(menu: Menu) {
      menu[1].apply { // Get() doneButton
        isEnabled = viewModel.doneButtonEnabled.value!! // Since invalidate() called from observe, value should be ready to use
        icon?.alpha = if (isEnabled) 255 else 130 // Full brightness when enabled. Just over half when disabled
      }
    }
    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
      R.id.set_filters_action -> {
        val selectedFilters = ArrayList(viewModel.selectedFilterList.value ?: listOf())
        // setFragmentResult delivers its result to ReportList listener! BUT only once it's in STARTED lifecycle state
        setFragmentResult(SortFilterRequestKey, bundleOf(SelectedFilterParcel to selectedFilters))
        findNavController().navigateUp() // Pop off this fragment like parentFragmentManager.popBackStack() or activity.finish()
        true
      }
      R.id.reset_filters_action -> {
        viewModel.resetFilters().forEach { index -> expandableFilterAdapter.notifyItemChanged(index) }
        true
      }
      else -> onNavDestinationSelected(item, findNavController())
    }
  }

  private fun setupExpandableRecyclerView() { // ExpandableListView = alt choice BUT RecyclerViews save on memory
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
    val (precautionNames, healthPracticeNames) = activityViewModel.getNamesLists()
    viewModel.initializeFilterList(precautionNames, healthPracticeNames)
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
      layoutManager = FlexboxLayoutManager(context).apply { justifyContent = JustifyContent.CENTER }
    }
    viewModel.selectedFilterList.observe(viewLifecycleOwner) { newList -> selectedFilterAdapter.submitList(newList) }
  }

  private inner class FilterSelectionListener : OnFilterSelectedListener {
    override fun onFilterSelected(view: View, selectedFilter: FilterItem, singleSelectionEnabled : Boolean) {
      // Unmark any previous radiobutton selected or if filter already selected. Add the newly selected filter to adapter
      val (removalIndex, addedIndex) = viewModel.selectFilter(selectedFilter, singleSelectionEnabled)
      if (removalIndex != -1) { selectedFilterAdapter.notifyItemRemoved(removalIndex) }
      if (addedIndex != -1) { selectedFilterAdapter.notifyItemInserted(addedIndex) }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _viewBinding = null
  }
}