package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.adapters.ReportAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.adapters.SelectedFilterAdapter
import kotlin.collections.ArrayList
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityReportListBinding
// Following = FetchReports dependencies TODO Move to a Service class
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.preSelectedFilterExtra
import edu.usc.nlcaceres.infectionprevention.util.selectedFilterParcel
import edu.usc.nlcaceres.infectionprevention.util.reportsURL
import edu.usc.nlcaceres.infectionprevention.util.RequestQueueSingleton
import edu.usc.nlcaceres.infectionprevention.util.ReportDeserializer
import edu.usc.nlcaceres.infectionprevention.util.MAX_RETRIES
import edu.usc.nlcaceres.infectionprevention.util.TIMEOUT_MS
import edu.usc.nlcaceres.infectionprevention.util.ReportListCancelTag

/* Main Activity of App: Lists all reports returned using a complex RecyclerView
 Moves to a Sorting/Filter Activity (ActivitySortFilter) on click of Floating Filter Button
 Easily one of the most complex views */
class ActivityReportList : AppCompatActivity() {

  private lateinit var viewBinding : ActivityReportListBinding

  private lateinit var toolbar : Toolbar

  private val handler = Handler(Looper.getMainLooper())
  private var runnable = Runnable {}

  private lateinit var refreshLayout : SwipeRefreshLayout

  private lateinit var sorryMessage : TextView

  private lateinit var filterFloatButton : FloatingActionButton
  private lateinit var selectedFilterRV : RecyclerView
  private val selectedFilters = arrayListOf<FilterItem>()
  private lateinit var selectedFilterAdapter : SelectedFilterAdapter

  private lateinit var reportsRV : RecyclerView
  private val reportList : ArrayList<Report> = arrayListOf()
  private lateinit var reportsAdapter : ReportAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityReportListBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    toolbar = SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_back_arrow)

    refreshLayout = viewBinding.swipeLayout.apply {
      setColorSchemeResources(R.color.colorPrimary, R.color.colorLight)
      setOnRefreshListener { fetchReports() }
    }

    intent.getParcelableExtra<FilterItem>(preSelectedFilterExtra)?.let { selectedFilters.add(it) }

    selectedFilterAdapter = SelectedFilterAdapter { _, _, position -> // View, FilterItem, Int
      selectedFilters.removeAt(position) // First remove filter from selectedFilterList
      selectedFilterAdapter.notifyItemRemoved(position) // Bit more efficient than submitting whole new list to diff
      beginSortAndFiltering()
    }
    selectedFilterRV = viewBinding.selectedFilterRV.apply {
      adapter = selectedFilterAdapter // May be worth using apply with submitList in it!
      (adapter as SelectedFilterAdapter).submitList(selectedFilters)
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }

    sorryMessage = viewBinding.sorryTextView // Fallback textview

    reportsRV = viewBinding.reportRV.apply {
      reportsAdapter = ReportAdapter().also { adapter = it } // ALSO setAdapter for reportsRV
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    if (reportList.isEmpty()) { refreshLayout.isRefreshing = true; fetchReports() }

    filterFloatButton = viewBinding.sortFilterFloatingButton.apply { setOnClickListener {
      Intent(context, ActivitySortFilter::class.java).let {
        it.putStringArrayListExtra("PrecautionList", intent?.getStringArrayListExtra("PrecautionList"))
        it.putStringArrayListExtra("PracticeList", intent?.getStringArrayListExtra("PracticeList"))
        sortFilterActivityLauncher.launch(it) // How to launch new Activity as of SDK 30 (no more request codes)
      }
    }}
  }

  override fun onBackPressed() { // Guaranteed to fire (since up+back are only way out) and immediately! Normally would be onStop
    // Application Context better below! No leaky activities!
    RequestQueueSingleton.getInstance(applicationContext).requestQueue.cancelAll(ReportListCancelTag)
    refreshLayout.isRefreshing = true
    super.onBackPressed() // Calling the super ensures activity calls finish()
  }

  // Set up searchButton + Settings button
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.report_list_actions, menu)
    setUpSearchView(menu)
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.settings_action -> { startActivity(Intent(this, ActivitySettings::class.java)); true }
    android.R.id.home -> { onBackPressed(); true }
    else -> super.onOptionsItemSelected(item)
  }

  private fun setUpSearchView(menu : Menu) {
    val searchMenu = menu.findItem(R.id.search_action)
    val searchBar = (searchMenu?.actionView as EditText).apply {
      setHint(R.string.search_hint)
      setHintTextColor(ContextCompat.getColor(this@ActivityReportList, R.color.colorLight))
      maxLines = 1; inputType = TYPE_CLASS_TEXT // Need BOTH of these to restrict text to single line
      background.colorFilter = PorterDuffColorFilter(ContextCompat
        .getColor(this@ActivityReportList, R.color.colorLight), PorterDuff.Mode.SRC_IN)
    }
    val focusListener = View.OnFocusChangeListener { view, isFocused ->
      if (!isFocused) { // Whenever searchActionView loses focus close keyboard + close actionView if no query
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(view.windowToken, 0)
        (view as? EditText)?.run { if (text.isEmpty()) toolbar.collapseActionView() }
      }
    }
    val expansionListener = object : MenuItem.OnActionExpandListener {
      override fun onMenuItemActionExpand(searchIcon: MenuItem?): Boolean { // Returning true lets expansion/collapse to happen
        searchBar.requestFocus()
        if (searchBar.onFocusChangeListener == null) searchBar.onFocusChangeListener = focusListener
        return true
      }
      override fun onMenuItemActionCollapse(searchIcon: MenuItem?): Boolean {
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
          .hideSoftInputFromWindow(searchIcon?.actionView?.windowToken, 0)
        searchBar.text.clear(); return true
      }
    }
    searchMenu.setOnActionExpandListener(expansionListener)

    searchBar.doOnTextChanged { newText, _, _, _ -> // newText defaults to "" (never null)
      EspressoIdlingResource.increment() // Slows instrumentedTests due to delay below but now tests work as expected!
      // Alternative is using kotlin coroutines, using a job launched off Dispatchers.Main with a delay and func passed in (so it's also cancellable)
      handler.removeCallbacks(runnable) // Idea is to kill previous runnables on next text change
      runnable = Runnable { filterReportsBySearch(newText.toString()) } // Order of remove and then setting runnable matters here!
      handler.postDelayed(runnable, 750) // There4 this will only run if half a second gap in text change
    }
  }

  private fun filterReportsBySearch(searchText: String) {
    if (searchText.isEmpty()) {
      // Instead of an updateData() in the adapter, the new ListAdapter has submitList() which autodiffs on a background thread if RV not empty!
      reportsAdapter.submitList(reportList)
      if (reportsAdapter.itemCount > 0) reportsRV.scrollToPosition(0) // Checking after update
    }
    else {
      val sortedList = reportList.filter { report -> // If case sensitive, then `str2 in str1` beats str1.contain(str2)
        (report.employee?.fullName?.contains(searchText, true) ?: false) ||
          report.date.toString().contains(searchText, true) ||
          report.location.toString().contains(searchText, true) ||
          (report.healthPractice?.name?.contains(searchText, true) ?: false)
      }
      reportsAdapter.submitList(sortedList)
    }
    EspressoIdlingResource.decrement()
  }

  // TODO: Allow multiple formats to be compared
  //private fun dateSearchComparison(date : Date, searchText : String) {}

  private fun fetchReports() {
    EspressoIdlingResource.increment()
    val reportsListRequest = StringRequest(reportsURL, {
      val newReportsList : ArrayList<Report> = arrayListOf()
      try {
        newReportsList.addAll(GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer()).create().
          fromJson(it, TypeToken.getParameterized(ArrayList::class.java, Report::class.java).type))
      } catch (err : Error) { Log.w("Report Parse Err", "Issue with parsing the json for reports") }

      if (reportList.isNotEmpty()) reportList.clear() // Clear any previous calls' returns
      reportList.addAll(newReportsList) // Activity also needs a copy
      reportsAdapter.submitList(newReportsList) // notifyDataSetChanged no longer needed (and should never have been)
      beginSortAndFiltering()
      refreshLayout.isRefreshing = false
      EspressoIdlingResource.decrement()

    }, {
      Log.w("Report Fetch Err", "Issue fetching reports")
      val alertDialog = AppFragmentAlertDialog.newInstance(resources.getString(R.string.main_alert_dialog_title),
        resources.getString(R.string.report_list_alert_dialog_message), false)
      alertDialog.show(supportFragmentManager, "main_alert_dialog")

      refreshLayout.isRefreshing = false
      sorryMessage.visibility = View.VISIBLE
      EspressoIdlingResource.decrement()
    })

    reportsListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    reportsListRequest.tag = ReportListCancelTag
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(reportsListRequest)
  }

  /* How to handle Activity switches as of SDK 30 */
  private val sortFilterActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    // Handles result from the Sorting/Filtering Activity
    if (it.resultCode == Activity.RESULT_OK) {
      val selectedFiltersReceived = it.data?.getParcelableArrayListExtra<FilterItem>(selectedFilterParcel)
      if (selectedFiltersReceived != null && selectedFiltersReceived.isNotEmpty()) {
        Snackbar.make(viewBinding.myCoordinatorLayout, "Filtering and Sorting!", Snackbar.LENGTH_SHORT).show()

        selectedFilters.clear(); selectedFilters.addAll(selectedFiltersReceived)
        selectedFilterAdapter.notifyItemRangeInserted(0, selectedFiltersReceived.size)

        beginSortAndFiltering()
      }
    }
  }

  private fun beginSortAndFiltering(reportsList: List<Report> = reportList, chosenFilters: List<FilterItem> = selectedFilters) {
    val onlySortNeeded = chosenFilters.size == 1 && chosenFilters[0].filterGroupName == "Sort By" // Helps avoid filtering but returning true for everything
    var sortByIndex = if (onlySortNeeded) 0 else -1
    val filteredList = if (chosenFilters.isEmpty() || onlySortNeeded) reportsList
      else reportsList.filter { report ->
        var keepReport = false
        chosenFilters.forEachIndexed { i, filter ->
          if (filter.filterGroupName == "Sort By") sortByIndex = i
          else {
            keepReport = filterReportsBy(report, filter.filterGroupName, filter.name)
            if (keepReport) return@filter keepReport // Early break if we already have a match
          }
        }
        return@filter keepReport
      }
    // If we found a sort instruction then call sort so it can return a sorted list to submit! Else submit the filteredList
    if (sortByIndex in chosenFilters.indices) reportsAdapter.submitList(sortReportsBy(filteredList, chosenFilters[sortByIndex].name))
    else reportsAdapter.submitList(filteredList)
  }

  private fun filterReportsBy(report: Report, filterCategory: String, filter: String) = when (filterCategory) {
    "Precaution Type" -> report.healthPractice?.precaution?.name?.startsWith(filter)
    "Health Practice Type" -> report.healthPractice?.name?.startsWith(filter)
    else -> false // Unknown filter, no match. Allow empty results (if results empty, use sorryTextView message?)
  } ?: false // If a null chain fails, returning null, then Elvis fallback to non-null false

  private fun sortReportsBy(filteredList: List<Report>, sorter: String) = when (sorter) {
    "Date Reported" -> filteredList.sortedBy { it.date }
    "Employee Name (A-Z)" -> filteredList.sortedBy { it.employee?.fullName }
    "Employee Name (Z-A)" -> filteredList.sortedByDescending { it.employee?.fullName }
    else -> filteredList
  }
}