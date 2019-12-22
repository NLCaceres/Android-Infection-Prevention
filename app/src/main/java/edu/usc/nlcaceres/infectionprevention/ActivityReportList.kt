package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import edu.usc.nlcaceres.infectionprevention.helpers.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_report.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityReportList : AppCompatActivity() {

  private lateinit var toolbar : Toolbar

  private val handler = Handler()
  private var runnable = Runnable {}

  private lateinit var refreshLayout : SwipeRefreshLayout

  private lateinit var sorryMessage : TextView

  private lateinit var selectedFilterAdapter : AdapterSelectedFilterRV
  private lateinit var selectedFilterRV : RecyclerView
  private val selectedFilters = arrayListOf<FilterItem>()
  private lateinit var filterFloatButton : FloatingActionButton

  private lateinit var reportsRV : RecyclerView
  private val reportList : ArrayList<Report> = arrayListOf()
  private lateinit var reportsAdapter : ReportsRVAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_report_list)

    toolbar = findViewById(R.id.home_toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow) // This will override any styling
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    refreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeLayout).apply {
      setColorSchemeResources(R.color.colorPrimary, R.color.colorLight)
      setOnRefreshListener { fetchReports() }
    }

    val preSelectedFilter = intent.getParcelableExtra<FilterItem>(preSelectedFilterExtra)
    if (preSelectedFilter != null) { selectedFilters.add(preSelectedFilter) }
    selectedFilterAdapter = AdapterSelectedFilterRV(selectedFilters, RemoveFilterListener())
    selectedFilterRV = findViewById<RecyclerView>(R.id.selectedFilterRV).apply {
      adapter = selectedFilterAdapter; visibility = if (selectedFilters.size > 0) View.VISIBLE else View.GONE
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply { justifyContent = JustifyContent.CENTER }
    }

    filterFloatButton = findViewById<FloatingActionButton>(R.id.sortFilterFloatingButton).apply { setOnClickListener {
      val sortFilterIntent = Intent(context, ActivitySortFilter::class.java)
      sortFilterIntent.putStringArrayListExtra("PrecautionList", intent?.getStringArrayListExtra("PrecautionList"))
      sortFilterIntent.putStringArrayListExtra("PracticeList", intent?.getStringArrayListExtra("PracticeList"))
      startActivityForResult(sortFilterIntent, 24)
    }}

    sorryMessage = findViewById(R.id.sorryTextView)

    reportsRV = findViewById<RecyclerView>(R.id.reportRV).apply {
      reportsAdapter = ReportsRVAdapter(arrayListOf()) // Important! If set to the main list (reportList) then they'll ref same memory and cause odd bugs! - so let updateData do the work!
      adapter = reportsAdapter
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    if (reportList.size == 0) refreshLayout.isRefreshing = true; fetchReports()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.report_list_actions, menu)

    menu?.let { setUpSearchView(menu) }

    return true
  }

  private fun setUpSearchView(menu : Menu) {
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

    val searchMenu = menu.findItem(R.id.search_action).apply {
      icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorLight), PorterDuff.Mode.SRC_IN)
    }

    val searchView = (searchMenu?.actionView as SearchView).apply { setSearchableInfo(searchManager.getSearchableInfo(componentName)) } // searchInfo sets config in /res/xml module
    val searchBox = searchView.findViewById<AutoCompleteTextView>(androidx.appcompat.R.id.search_src_text).apply {
      setOnFocusChangeListener { view, focused ->
        if (!focused) (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        if (this.text.isEmpty()) toolbar.collapseActionView() // Full closure only with empty searchViewTextbox
      }
      setHintTextColor(ContextCompat.getColor(applicationContext, R.color.appBackground))
    }
    searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn).apply {
      setOnClickListener { // Mostly what normally happens (with a bonus full closure)
        searchBox.setText(""); searchView.setQuery("", false) // Query is emptied and not sent out
        searchView.onActionViewCollapsed(); searchMenu.collapseActionView() // Close actionView then iconify widget
      } // If needed tie in with searchMenu.setOnActionExpandListener (which has funcs for expansion and closure)
    }

    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextChange(newText: String): Boolean { // NewText starts with "" (never null)
        // Alternative is using kotlin coroutines, specifying a job launched off Dispatchers.Main with a delay and a function passed in (so it's also cancellable)
        handler.removeCallbacks(runnable) // Idea is to kill previous runnables on next text change
        runnable = Runnable { filterReportsBySearch(newText) } // Order of remove and then setting runnable matters here!

        handler.postDelayed(runnable, 750) // There4 this will only run if half a second gap in text change

        return false // False == default suggestions by searchView instead of true == by listener
      }
      override fun onQueryTextSubmit(query: String?): Boolean = false // False == Handle by SearchView instead of by listener
    })
  }

  private fun filterReportsBySearch(searchText: String) { // Alternatively place in adapter! with copy of array init'd by addAll. toLowercase may help!
    if (searchText.isEmpty()) {
      reportsAdapter.updateData(reportList)
      if (reportsAdapter.itemCount > 0)reportsRV.scrollToPosition(0) // Checking after update
    } else {
      val sortedList = reportList.filter { report -> // If ignoreCase = false is ok, then String2 in String1 is a valid expression in kotlin!
        report.employee?.fullName?.contains(searchText, true)!! ||
            report.date.toString().contains(searchText, true) ||
            report.location.toString().contains(searchText, true) ||
            report.healthPractice?.name?.contains(searchText, true)!!
      } as ArrayList<Report>
      reportsAdapter.updateData(sortedList)
    }
  }

  private fun dateSearchComparison(date : Date, searchText : String) { // TODO: Allow multiple formats to be compared

  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.settings_action -> { startActivity(Intent(this, ActivitySettings::class.java)); true }
    android.R.id.home -> { onBackPressed(); true }
    else -> super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() { // Guaranteed to fire and immediately! - normally would put the following in onStop
    RequestQueueSingleton.getInstance(applicationContext).requestQueue.cancelAll(ReportListCancelTag) // Application Context better in this case! No leaking activities!
    refreshLayout.isRefreshing = true
    super.onBackPressed() // Calling the super ensures activity finish occurs
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == ReportListRequestCode) {
      if (resultCode == Activity.RESULT_OK) {
        val selectedFilters = data?.getParcelableArrayListExtra<FilterItem>(selectedFilterParcel)
        if (selectedFilters != null && selectedFilters.size > 0) {
          Toast.makeText(this, "Got filters!", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) { // If activity is singleTop launchMode then this is needed (activity not recreated so no onCreate to grab searchIntent)
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

  private fun fetchReports() {
    val reportsListRequest = StringRequest(reportsURL, Response.Listener {
      val newReportsList : ArrayList<Report> = arrayListOf()
      try {
        newReportsList.addAll(GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer()).create().
            fromJson(it, TypeToken.getParameterized(ArrayList::class.java, Report::class.java).type))
      } catch (err : Error) { Log.w("Report Parse Err", "Issue with parsing the json for reports")}
      if (reportList.size > 0 ) reportList.clear(); reportList.addAll(newReportsList) // Important for keeping full list reference
      reportsAdapter.updateData(newReportsList)
      reportsAdapter.notifyDataSetChanged()
      refreshLayout.isRefreshing = false

    }, Response.ErrorListener {
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

  private inner class RemoveFilterListener : AdapterSelectedFilterRV.RemoveFilterListener {
    override fun onRemoveButtonClicked(view: View, filter: FilterItem, position: Int) {

    }
  }

  private class ReportsRVAdapter(private val reportsList : ArrayList<Report>) : RecyclerView.Adapter<ReportsRVAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder = ReportViewHolder(LayoutInflater.
        from(parent.context).inflate(R.layout.item_report, parent, false))

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
      holder.bind(reportsList[position])
    }

    private class ReportViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
      fun bind(report : Report) {
        //reportTypeImageView
        val reportTypeText = "${report.healthPractice?.name} Violation"
        reportTypeTitleTV.text = reportTypeText
        reportDateTV.text = report.date?.let { SimpleDateFormat("MMM dd, yyyy h:mma", Locale.getDefault()).format(it) }
        val reportEmployeeText = "Committed by ${report.employee?.fullName}"
        reportEmployeeNameTV.text = reportEmployeeText
        val locationText = "Location: ${report.location?.facilityName} Unit: ${report.location?.unitNum} Room: ${report.location?.roomNum}"
        reportLocationTV.text = locationText
      }
    }
    override fun getItemCount(): Int = reportsList.size

    fun updateData(newReports: ArrayList<Report>) {
      val diffResults = DiffUtil.calculateDiff(ReportDiffCallback(reportsList, newReports))

      reportsList.clear()
      reportsList.addAll(newReports) // If it's all the same these two are all that's needed to prevent jumpy UI

      diffResults.dispatchUpdatesTo(this)
    }
  }

  private class ReportDiffCallback(private val oldList : ArrayList<Report>, private val newList : ArrayList<Report>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == (newList[newItemPosition])
      // Called only if areItems fun returns TRUE // Alternatively destructure and compare as needed
//      val (_, oldEmployee, oldPractice, oldLocation, oldDate) = oldList[oldItemPosition]
//      val (_, newEmployee, newPractice, newLocation, newDate) = newList[oldItemPosition]
  }
}