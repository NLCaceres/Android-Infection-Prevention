package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
//import java.edu.usc.nlcaceres.infectionprevention.util.Date
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

    selectedFilterAdapter = SelectedFilterAdapter { _, _, _ -> // TODO: RemoveButtonListener

    }
    selectedFilterRV = viewBinding.selectedFilterRV.apply {
      // If our custom Adapter doesn't receive a brand new list then its updateData callback can act odd
      adapter = selectedFilterAdapter // May be worth using apply with submitList in it!
      (adapter as SelectedFilterAdapter).submitList(selectedFilters)
      visibility = if (selectedFilters.size > 0) View.VISIBLE else View.GONE
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }
    sorryMessage = viewBinding.sorryTextView // Fallback textview
    reportsRV = viewBinding.reportRV.apply {
      //TODO May need to pass a callback! (updateData() fun in the file! maybe?)
      reportsAdapter = ReportAdapter().also { adapter = it } // ALSO set adapter of reportsRV
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    if (reportList.size == 0) { refreshLayout.isRefreshing = true; fetchReports() }

    filterFloatButton = viewBinding.sortFilterFloatingButton.apply { setOnClickListener {
      Intent(context, ActivitySortFilter::class.java).let {
        it.putStringArrayListExtra("PrecautionList", intent?.getStringArrayListExtra("PrecautionList"))
        it.putStringArrayListExtra("PracticeList", intent?.getStringArrayListExtra("PracticeList"))
        sortFilterActivityLauncher.launch(it) // How to handle Activity changes as of SDK 30 (no more request codes)
      }
    }}
  }

  override fun onBackPressed() { // Guaranteed to fire and immediately! - normally would put the following in onStop
    // Application Context better below! No leaky activities!
    RequestQueueSingleton.getInstance(applicationContext).requestQueue.cancelAll(ReportListCancelTag)
    refreshLayout.isRefreshing = true
    super.onBackPressed() // Calling the super ensures activity finish occurs
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
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

    val searchMenu = menu.findItem(R.id.search_action).apply {
      icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorLight), PorterDuff.Mode.SRC_IN)
    }

    // searchInfo sets config in /res/xml module
    val searchView = (searchMenu?.actionView as SearchView).apply { setSearchableInfo(searchManager.getSearchableInfo(componentName)) }

    // Next 3 blocks set up interactivity of searchView components (autocomplete box + close button + editText)
    val searchBox = searchView.findViewById<AutoCompleteTextView>(androidx.appcompat.R.id.search_src_text).apply {
      setHintTextColor(ContextCompat.getColor(applicationContext, R.color.appBackground))
      setOnFocusChangeListener { view, focused ->
        if (!focused) (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        if (text.isEmpty()) toolbar.collapseActionView() // Close searchView entirely only if empty searchViewTextbox
      }
    }
    searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn).apply {
      setOnClickListener { // Mostly what normally happens (with a bonus full closure)
        searchBox.setText(""); searchView.setQuery("", false) // Query is emptied and not sent out
        searchView.onActionViewCollapsed(); searchMenu.collapseActionView() // Close actionView then reset widget to icon form
      } // If needed tie in with searchMenu.setOnActionExpandListener (which has funcs for expansion and closure)
    }
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextChange(newText: String): Boolean { // NewText's default = "" (never null)
        // Alternative is using kotlin coroutines, using a job launched off Dispatchers.Main with a delay and func passed in (so it's also cancellable)
        handler.removeCallbacks(runnable) // Idea is to kill previous runnables on next text change
        runnable = Runnable { filterReportsBySearch(newText) } // Order of remove and then setting runnable matters here!
        handler.postDelayed(runnable, 750) // There4 this will only run if half a second gap in text change
        return false // False == default suggestions by searchView instead of true == by listener
      }
      override fun onQueryTextSubmit(query: String?): Boolean = false // False == Handle by SearchView instead of by listener
    })
  }

  // If activity = singleTopLaunchMode then need this for searchView - activity not recreated so no onCreate to grab previous searchIntent
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent) // Ensures all the right things fire under the hood
    if (Intent.ACTION_SEARCH == intent.action) { // Prevents back button from sort and filter list from overwriting intents
      // setIntent(intent) // If not called, then old intent is kept (which would be useful if needed later)
      intent.getStringExtra(SearchManager.QUERY)?.also { query ->
        handler.removeCallbacks(runnable)
        runnable = Runnable { filterReportsBySearch(query) }
        handler.postDelayed(runnable, 750)
      }
    }
  }

  private fun filterReportsBySearch(searchText: String) { // Alternatively place in adapter! with copy of array init'd by addAll.
    if (searchText.isEmpty()) {
      // Instead of an updateData() in the adapter, the new ListAdapter has submitList() which autodiffs on a background thread if the RV isn't empty!
      reportsAdapter.submitList(reportList)
      if (reportsAdapter.itemCount > 0) reportsRV.scrollToPosition(0) // Checking after update
    }
    else {
      val sortedList = reportList.filter { report -> // If case sensitive, then `str2 in str1` beats str1.contain(str2)
        report.employee?.fullName?.contains(searchText, true)!! ||
            report.date.toString().contains(searchText, true) ||
            report.location.toString().contains(searchText, true) ||
            report.healthPractice?.name?.contains(searchText, true)!!
      } as ArrayList<Report>
      reportsAdapter.submitList(sortedList)
    }
  }

  // TODO: Allow multiple formats to be compared
  //private fun dateSearchComparison(date : Date, searchText : String) {}

  private fun fetchReports() {
    val reportsListRequest = StringRequest(reportsURL, {
      val newReportsList : ArrayList<Report> = arrayListOf()
      try {
        newReportsList.addAll(GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer()).create().
          fromJson(it, TypeToken.getParameterized(ArrayList::class.java, Report::class.java).type))
      } catch (err : Error) { Log.w("Report Parse Err", "Issue with parsing the json for reports")}

      if (reportList.size > 0 ) reportList.clear(); reportList.addAll(newReportsList) // Activity's copy of what adapter gets
      reportsAdapter.submitList(newReportsList) // notifyDataSetChanged no longer needed (and should never have been)
      refreshLayout.isRefreshing = false

    }, {
      Log.w("Report Fetch Err", "Issue fetching reports")
      val alertDialog = AppFragmentAlertDialog.newInstance(resources.getString(R.string.main_alert_dialog_title),
        resources.getString(R.string.report_list_alert_dialog_message), false)
      alertDialog.show(supportFragmentManager, "main_alert_dialog")

      refreshLayout.isRefreshing = false
      sorryMessage.visibility = View.VISIBLE
    })

    reportsListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    reportsListRequest.tag = ReportListCancelTag
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(reportsListRequest)
  }

  /* How to handle Activity switches as of SDK 30 */
  private val sortFilterActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    // Handles result from the Sorting/Filtering Activity
    if (it.resultCode == Activity.RESULT_OK) {
      val selectedFilters = it.data?.getParcelableArrayListExtra<FilterItem>(selectedFilterParcel)
      if (selectedFilters != null && selectedFilters.size > 0) {
        Snackbar.make(viewBinding.myCoordinatorLayout, "Got filters!", Snackbar.LENGTH_SHORT).show()
      }
    }
  }
}