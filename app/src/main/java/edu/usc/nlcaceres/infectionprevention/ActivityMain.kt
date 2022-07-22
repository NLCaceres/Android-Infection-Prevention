package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.ReportService
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityMainBinding
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.createReportPracticeExtra
import edu.usc.nlcaceres.infectionprevention.util.preSelectedFilterExtra
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import retrofit2.HttpException

/* Homepage that allows users to choose a type of health violation that occurred
 and create a report for it
 Links to: ActivityCreateReport + ActivityReportList */
//TODO: Fix recreation when going from CreateReport or Settings back to Main. ReportsList and even Filters work fine
class ActivityMain : AppCompatActivity() {

  private lateinit var viewBinding : ActivityMainBinding
  private lateinit var mDrawerLayout: DrawerLayout
  private lateinit var mProgressIndicator : ProgressBar
  private lateinit var sorryMsgTextView : TextView
  private lateinit var precautionRecyclerView : RecyclerView
  private lateinit var precautionAdapter : PrecautionAdapter
  private var precautionFetchJob : Job? = null
  private var precautionList = arrayListOf<Precaution>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    setUpToolbarAndNavView()
    sorryMsgTextView = viewBinding.sorryTextView

    precautionRecyclerView = viewBinding.precautionRV.apply {
      setHasFixedSize(true)
      precautionAdapter = PrecautionAdapter { _, healthPractice ->
        // Click Listener that creates an intent and launches the CreateReport Activity
        Intent(applicationContext, ActivityCreateReport::class.java).apply {
          putExtra(createReportPracticeExtra, healthPractice.name)
        }.also { createReportActivityLauncher.launch(it) }
      }
      adapter = precautionAdapter
      EspressoIdlingResource.increment()
      precautionAdapter.submitList(precautionList)
      EspressoIdlingResource.decrement()
    }
    if (precautionList.size == 0) fetchPrecautions()
  }

  private fun setUpToolbarAndNavView() {
    // Since MainLauncher Activity label gives the app icon AND the toolbar its title, we purposely remove it here
    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_menu, " ")

    mDrawerLayout = viewBinding.myNavDrawer

    viewBinding.navView.apply {
      setNavigationItemSelectedListener(NavDrawerHandler())
      getHeaderView(0).findViewById<Button>(R.id.navCloseButton).setOnClickListener { mDrawerLayout.closeDrawers() }
    }

    mProgressIndicator = viewBinding.progressIndicatorLayout.appProgressbar.apply { visibility = View.VISIBLE }
  }

  /* Next 2 funs = Menu Setup */
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.action_buttons, menu)
    return true // super.onCreateOptionsMenu(menu) // If returns false, menu won't display
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean { // Return true to end any further menu processing
    return when (item.itemId) {
      android.R.id.home -> { mDrawerLayout.openDrawer(GravityCompat.START); true }
      R.id.action_settings -> {
        Snackbar.make(viewBinding.myCoordinatorLayout, R.string.my_message, Snackbar.LENGTH_SHORT).show()
        Intent(this, ActivitySettings::class.java).also { startActivity(it) }
        true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  private val createReportActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      startActivity(Intent(applicationContext, ActivityReportList::class.java))
    }
  }

  private fun fetchPrecautions() {
    EspressoIdlingResource.increment()
    precautionFetchJob = CoroutineScope(Dispatchers.IO).launch {
      val precautionResponse = ReportService.createPrecautionApi().fetchPrecautionList()
      withContext(Dispatchers.Main) {
        try {
          if (precautionResponse.isSuccessful) {
            // Using submitList callback to add new list of items once committed to activity's list
            precautionResponse.body()?.let { precautionAdapter.submitList(ArrayList(it)) {
              precautionList.addAll(it)
            }}
          }
          else {
            Snackbar.make(viewBinding.myCoordinatorLayout, "Error: ${precautionResponse.code()}", Snackbar.LENGTH_SHORT).show()
            AppFragmentAlertDialog.newInstance(resources.getString(R.string.main_alert_dialog_title), resources.getString(R.string.main_alert_dialog_message),
              false).show(supportFragmentManager, "main_alert_dialog")
            sorryMsgTextView.visibility = View.VISIBLE
          }
          EspressoIdlingResource.decrement()
          mProgressIndicator.visibility = View.INVISIBLE // Made invisible no matter what
        } catch (e : HttpException) {
          Snackbar.make(viewBinding.myCoordinatorLayout, "HTTP Error: ${e.message()}", Snackbar.LENGTH_SHORT).show()
          EspressoIdlingResource.decrement()
        } catch (e : Throwable) {
          Snackbar.make(viewBinding.myCoordinatorLayout, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
          EspressoIdlingResource.decrement()
        }
      }
    }
  }

  private inner class NavDrawerHandler : NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
      val reportListIntent = Intent(applicationContext, ActivityReportList::class.java)
      when (item.itemId) {
        R.id.nav_reports -> {
          Log.d("Nav Item Selected", "Clicked General Report Item")
        }
        R.id.nav_standard_precautions -> {
          reportListIntent.putExtra(preSelectedFilterExtra, FilterItem("Standard", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Standard Precautions Report Item")
        }
        R.id.nav_isolation_precautions -> {
          reportListIntent.putExtra(preSelectedFilterExtra, FilterItem("Isolation", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Isolation Precautions Report Item")
        }
        else -> return false
      }
      mDrawerLayout.closeDrawers()

      val precautionNames = arrayListOf<String>()
      val healthPracticeNames = arrayListOf<String>()
      for (currentPrecaution in precautionList) {
        precautionNames.add(currentPrecaution.name)
        currentPrecaution.practices?.let { healthPracticeList ->
          for (currentHealthPractice in healthPracticeList) healthPracticeNames.add(currentHealthPractice.name)
        }
      }
      reportListIntent.putStringArrayListExtra("PrecautionList", precautionNames)
      reportListIntent.putStringArrayListExtra("PracticeList", healthPracticeNames)
      startActivity(reportListIntent)
      return true
    }
  }

  override fun onStop() {
    super.onStop()
    precautionFetchJob?.cancel() // Kill pending coroutines
  }
}