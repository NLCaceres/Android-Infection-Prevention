package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityReportListBinding
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.fetchParcelable
import edu.usc.nlcaceres.infectionprevention.util.fetchParcelableList
import edu.usc.nlcaceres.infectionprevention.util.IsDarkMode
import edu.usc.nlcaceres.infectionprevention.util.preSelectedFilterExtra
import edu.usc.nlcaceres.infectionprevention.util.selectedFilterParcel
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.ShowSnackbar
import edu.usc.nlcaceres.infectionprevention.util.textUpdates
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelReportList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

/* Main Activity of App: Lists all reports returned using a complex RecyclerView
 Moves to a Sorting/Filter Activity (ActivitySortFilter) on click of Floating Filter Button
 Easily one of the most complex views */
@AndroidEntryPoint
class ActivityReportList : AppCompatActivity() {

  private val viewModel: ViewModelReportList by viewModels()
  private lateinit var viewBinding : ActivityReportListBinding

  private lateinit var toolbar : Toolbar
  private lateinit var refreshLayout : SwipeRefreshLayout
  private lateinit var sorryMsgTextView : TextView

  private lateinit var filterFloatButton : FloatingActionButton
  private lateinit var selectedFilterRV : RecyclerView
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter

  private lateinit var reportsRV : RecyclerView
  private lateinit var reportsAdapter : ReportAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityReportListBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    // Common Views
    toolbar = SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_back_arrow)
    setupRefresh()
    setupToastMessage()

    // Lists
    setupSortFilterViews()
    setupReportRV()

    setupFloatButtonToSortFilterView() // Bottom Floating button to nav to SortFilterView
  }

  // Set up searchButton + Settings button
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.report_list_actions, menu)
    setUpSearchView(menu)
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.settings_action -> { startActivity(Intent(this, ActivitySettings::class.java)); true }
    android.R.id.home -> { finish(); true }
    else -> super.onOptionsItemSelected(item)
  }

  private fun setupRefresh() {
    refreshLayout = viewBinding.swipeLayout.apply {
      val colorMode = if (IsDarkMode(this@ActivityReportList)) R.color.lightMode else R.color.darkMode
      setColorSchemeResources(R.color.colorPrimary, R.color.colorLight, colorMode)
      setOnRefreshListener { viewModel.refreshReportList() }
    }
    viewModel.isLoading.observe(this) { loading -> refreshLayout.isRefreshing = loading }
  }

  private fun setupToastMessage() {
    sorryMsgTextView = viewBinding.sorryTextView // Fallback textview
    viewModel.toastMessage.observe(this) { message ->
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        with(sorryMsgTextView) {
          visibility = if (viewModel.reportListEmpty()) View.VISIBLE else View.INVISIBLE
          text = message
        }
        ShowSnackbar(viewBinding.mainCoordinatorLayout, message, Snackbar.LENGTH_SHORT)
      }
    }
  }

  private fun setupSortFilterViews() {
    intent.fetchParcelable<FilterItem>(preSelectedFilterExtra)?.let { viewModel.selectedFilters = arrayListOf(it) }

    selectedFilterAdapter = SelectedFilterAdapter { _, _, position -> // RemoveButton() - View, FilterItem, Int
      viewModel.selectedFilters.removeAt(position) // First remove filter from selectedFilterList
      selectedFilterAdapter.notifyItemRemoved(position) // Bit more efficient than submitting whole new list to diff
      reportsAdapter.submitList(viewModel.sortedFilteredList())
    }

    selectedFilterRV = viewBinding.selectedFilterRV.apply {
      adapter = selectedFilterAdapter.apply { submitList(viewModel.selectedFilters) }
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
  }
  private fun setupReportRV() {
    EspressoIdlingResource.increment() // Decrement in onComplete block of viewModel

    reportsRV = viewBinding.reportRV.apply {
      reportsAdapter = ReportAdapter().also { adapter = it } // ALSO setAdapter for reportsRV
      val tintedLine = ContextCompat.getDrawable(context, R.drawable.custom_item_divider) // Get line drawable
        ?.apply { setTint(ContextCompat.getColor(context, R.color.colorPrimaryDark)) } // Color the line
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply { setDrawable(tintedLine!!) })
    }

    viewModel.reportState.observe(this) { (loading, newList) ->
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

  // SEARCHBAR in Toolbar - Called in toolbar createOptionsMenu
  private fun setUpSearchView(menu : Menu) {
    val searchMenu = menu.findItem(R.id.search_action)
    searchMenu.setOnActionExpandListener(ActionViewExpansionListener())

    (searchMenu?.actionView as EditText).apply {
      setHint(R.string.search_hint)
      setHintTextColor(ContextCompat.getColor(this@ActivityReportList, R.color.colorLight))

      maxLines = 1; inputType = TYPE_CLASS_TEXT // Need BOTH of these to restrict text to single line

      background.colorFilter = PorterDuffColorFilter(ContextCompat
        .getColor(this@ActivityReportList, R.color.colorLight), PorterDuff.Mode.SRC_IN)

    }.also { startSearchBarListener(it) }
  }

  // Needs access to inputManager via the activity so must be inner and declared in same file
  private inner class ActionViewExpansionListener: MenuItem.OnActionExpandListener {
    private val focusListener = View.OnFocusChangeListener { view, isFocused ->
      if (!isFocused) { // Whenever searchActionView loses focus close keyboard + close actionView if no query
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(view.windowToken, 0)
        (view as? EditText)?.run { if (text.isEmpty()) toolbar.collapseActionView() }
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
      if (currentFocus == searchBar && searchBar.text.isNotEmpty()) {
        searchBar.text.clear()
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(searchBar.windowToken, 0)
        searchBar.onFocusChangeListener = null // Prevents double collapse nullException call (listening restarts on next expansion)
        return true
      }
      else if (currentFocus == searchBar) {
        searchBar.clearFocus(); return false // Clear focus and let focusListener close actionView
      }
      // Made it here, either user didn't interact at all or above "else if" ran so clearFocus() let focusListener run collapse
      (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(searchIcon.actionView?.windowToken, 0)
      searchBar.text.clear(); return true
    }
  }

  @OptIn(FlowPreview::class)
  private fun startSearchBarListener(searchBar: EditText) {
    lifecycleScope.launch { // This flow is bound to the lifecycle of the activity!
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

  private fun setupFloatButtonToSortFilterView() {
    filterFloatButton = viewBinding.sortFilterFloatingButton.apply { setOnClickListener {
      Intent(context, ActivitySortFilter::class.java).let {
        it.putStringArrayListExtra("PrecautionList", intent?.getStringArrayListExtra("PrecautionList"))
        it.putStringArrayListExtra("PracticeList", intent?.getStringArrayListExtra("PracticeList"))
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
        ShowSnackbar(viewBinding.mainCoordinatorLayout, "Filtering and Sorting!", Snackbar.LENGTH_SHORT)

        viewModel.selectedFilters.clear()
        viewModel.selectedFilters.addAll(selectedFiltersReceived)
        selectedFilterAdapter.notifyItemRangeInserted(0, selectedFiltersReceived.size)

        reportsAdapter.submitList(viewModel.sortedFilteredList())
      }
    }
  }
}