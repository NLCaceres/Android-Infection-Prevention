package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.app.Activity
import androidx.fragment.app.Fragment
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import android.view.*
import android.widget.TextView
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import androidx.core.view.MenuProvider
import android.text.InputType.TYPE_CLASS_TEXT
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentReportListBinding
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelReportList
import edu.usc.nlcaceres.infectionprevention.util.*

/* Lists all reports returned using a complex RecyclerView - Easily one of the most complex views
 Navigates to a Sorting/Filter View (ActivitySortFilter) on click of Floating Filter Button */
@AndroidEntryPoint
class FragmentReportList: Fragment(R.layout.fragment_report_list) {
  private val viewModel: ViewModelReportList by viewModels()
  private var _viewBinding: FragmentReportListBinding? = null
  private val viewBinding get() = _viewBinding!!

  private lateinit var refreshLayout : SwipeRefreshLayout
  private lateinit var sorryMsgTextView : TextView

  private lateinit var filterFloatButton : FloatingActionButton
  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter

  private lateinit var reportsRV : RecyclerView
  private lateinit var reportsAdapter : ReportAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    EspressoIdlingResource.increment() // Decrement in onComplete block of viewModel
  }
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentReportListBinding.inflate(inflater, container, false)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (activity as AppCompatActivity).supportActionBar?.setUpIndicator(R.drawable.ic_back_arrow)

    requireActivity().addMenuProvider(ReportListMenu(), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setupRefresh()
    setupToastMessage()
    setupFloatButtonToSortFilterView()

    setupSortFilterViews()
    setupReportRV()
  }

  private fun setupRefresh() {
    refreshLayout = viewBinding.swipeLayout.apply {
      val colorMode = if (IsDarkMode(requireContext())) R.color.lightMode else R.color.darkMode
      setColorSchemeResources(R.color.colorPrimary, R.color.colorLight, colorMode)
      setOnRefreshListener { viewModel.refreshReportList() }
    }
    viewModel.isLoading.observe(viewLifecycleOwner) { loading -> refreshLayout.isRefreshing = loading }
  }
  private fun setupToastMessage() {
    sorryMsgTextView = viewBinding.sorryTextView // Fallback textview
    viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        with(sorryMsgTextView) {
          visibility = if (viewModel.reportListEmpty()) View.VISIBLE else View.INVISIBLE
          text = message
        }
        ShowSnackbar((activity as ActivityMain).coordinatorLayout, message, Snackbar.LENGTH_SHORT)
      }
    }
  }
  private fun setupFloatButtonToSortFilterView() {
    filterFloatButton = viewBinding.sortFilterFloatingButton.apply { setOnClickListener {
      Intent(context, ActivitySortFilter::class.java).let {
        it.putStringArrayListExtra(precautionListExtra, requireArguments().getStringArrayList(precautionListExtra))
        it.putStringArrayListExtra(healthPracticeListExtra, requireArguments().getStringArrayList(healthPracticeListExtra))
        sortFilterActivityLauncher.launch(it)
      }
    }}
  }
  /* How to handle Activity switches as of SDK 30 - Request Codes are gone! Just simple ResultCodes! */
  private val sortFilterActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    // Handles result from the Sorting/Filtering Activity
    if (it.resultCode == Activity.RESULT_OK) {
      val selectedFiltersReceived = it.data?.fetchParcelableList<FilterItem>(selectedFilterParcel)

      if (selectedFiltersReceived != null && selectedFiltersReceived.isNotEmpty()) {
        ShowSnackbar((activity as ActivityMain).coordinatorLayout, "Filtering and Sorting!", Snackbar.LENGTH_SHORT)

        viewModel.selectedFilters.clear()
        viewModel.selectedFilters.addAll(selectedFiltersReceived)
        selectedFilterAdapter.notifyItemRangeInserted(0, selectedFiltersReceived.size)

        reportsAdapter.submitList(viewModel.sortedFilteredList())
      }
    }
  }
  private fun setupSortFilterViews() {
    requireArguments().fetchParcelable<FilterItem>(preSelectedFilterExtra)?.let { viewModel.selectedFilters = arrayListOf(it) }

    selectedFilterAdapter = SelectedFilterAdapter { _, _, position -> // RemoveButton() - View, FilterItem, Int
      viewModel.selectedFilters.removeAt(position) // First remove filter from selectedFilterList
      selectedFilterAdapter.notifyItemRemoved(position) // Bit more efficient than submitting whole new list to diff
      reportsAdapter.submitList(viewModel.sortedFilteredList())
    }

    selectedFilterRV = viewBinding.selectedFilterRV.apply {
      adapter = selectedFilterAdapter.apply { submitList(viewModel.selectedFilters) }
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP)
        .apply { justifyContent = JustifyContent.CENTER }
    }
  }
  private fun setupReportRV() {
    reportsRV = viewBinding.reportRV.apply {
      reportsAdapter = ReportAdapter().also { adapter = it } // ALSO setAdapter for reportsRV
      val tintedLine = ContextCompat.getDrawable(context, R.drawable.custom_item_divider) // Get line drawable
        ?.apply { setTint(ContextCompat.getColor(context, R.color.colorPrimaryDark)) } // Color the line
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply { setDrawable(tintedLine!!) })
    }

    viewModel.reportState.observe(viewLifecycleOwner) { (loading, newList) ->
      reportsAdapter.submitList(viewModel.sortedFilteredList(newList))
      with(sorryMsgTextView) {
        text = when {
          loading -> "Looking up reports"
          newList.isEmpty() -> "Weird! Seems we don't have any available reports to view!"
          else -> "Please try again later!"
        }
        visibility = if (newList.isEmpty()) View.VISIBLE else View.INVISIBLE
      }
    }
  }

  private inner class ReportListMenu: MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      menuInflater.inflate(R.menu.report_list_actions, menu)
      setUpSearchView(menu)
    }
    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
      android.R.id.home -> { parentFragmentManager.popBackStack(); true } // Rather than requireActivity.finish()
      R.id.settings_action -> {
        parentFragmentManager.commit {
          setReorderingAllowed(true)
          addToBackStack(null)
          replace<FragmentSettings>(R.id.fragment_main_container)
        } // Rather than startActivity(intent, ActivitySettings)
        true
      }
      else -> false // False = Let something else handle the toolbar functionality
    }
    // SEARCHBAR in Toolbar - Called in toolbar createOptionsMenu
    private fun setUpSearchView(menu : Menu) {
      val searchMenu = menu.findItem(R.id.search_action)
      searchMenu.setOnActionExpandListener(ActionViewExpansionListener())

      (searchMenu?.actionView as EditText).apply {
        setHint(R.string.search_hint)
        setHintTextColor(ContextCompat.getColor(requireContext(), R.color.colorLight))

        maxLines = 1; inputType = TYPE_CLASS_TEXT // Need BOTH of these to restrict text to single line

        background.colorFilter = PorterDuffColorFilter(ContextCompat
          .getColor(requireContext(), R.color.colorLight), PorterDuff.Mode.SRC_IN)

      }.also { startSearchBarListener(it) }
    }
    @OptIn(FlowPreview::class)
    private fun startSearchBarListener(searchBar: EditText) {
      viewLifecycleOwner.lifecycleScope.launch { // This flow is bound to the lifecycle of the activity!
        searchBar.textUpdates().onEach { EspressoIdlingResource.increment() }
          .debounce(500) // No flowWithLifecycle() needed! unless we need to reset onStart
          .collectLatest { newText -> // Flow version of Handler w/ postDelayed + cancelling last Runnable block!
            // collectLatest kills previous computation if a new value arrives!

            if (newText.isNullOrBlank()) { reportsAdapter.submitList(viewModel.sortedFilteredList()) }
            else { reportsAdapter.submitList(viewModel.textFilteredList(newText.toString())) }

            if (reportsAdapter.itemCount > 0) reportsRV.scrollToPosition(0) // Checking after update
            EspressoIdlingResource.decrement()
          }
      }
    }
  }
  // Needs access to inputManager via the activity so must be inner and declared in same file
  private inner class ActionViewExpansionListener: MenuItem.OnActionExpandListener {
    private val focusListener = View.OnFocusChangeListener { view, isFocused ->
      if (!isFocused) { // Whenever searchActionView loses focus close keyboard + close actionView if no query
        (requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(view.windowToken, 0)
        (view as? EditText)?.run { if (text.isEmpty()) (requireActivity() as ActivityMain).toolbar.collapseActionView() }
      }
    }
    override fun onMenuItemActionExpand(searchIcon: MenuItem): Boolean { // Returning true lets expansion/collapse to happen
      val searchBar = searchIcon.actionView as EditText
      searchBar.requestFocus()
      if (searchBar.onFocusChangeListener == null) searchBar.onFocusChangeListener = focusListener
      return true
    }
    override fun onMenuItemActionCollapse(searchIcon: MenuItem): Boolean {
      val searchBar = searchIcon.actionView as EditText
      if (requireActivity().currentFocus == searchBar && searchBar.text.isNotEmpty()) {
        searchBar.text.clear()
        (requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(searchBar.windowToken, 0)
        searchBar.onFocusChangeListener = null // Prevents double collapse nullException call (listening restarts on next expansion)
        return true
      }
      else if (requireActivity().currentFocus == searchBar) {
        searchBar.clearFocus(); return false // Clear focus and let focusListener close actionView
      }
      // Made it here, either user didn't interact at all or above "else if" ran so clearFocus() let focusListener run collapse
      (requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(searchIcon.actionView?.windowToken, 0)
      searchBar.text.clear(); return true
    }
  }
}