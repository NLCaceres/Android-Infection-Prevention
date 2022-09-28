package edu.usc.nlcaceres.infectionprevention

import android.util.Log
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityMainBinding
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.util.ShowSnackbar
import edu.usc.nlcaceres.infectionprevention.util.createReportPracticeExtra
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

/* Homepage that allows users to choose a type of health violation that occurred
 and create a report for it
 Links to: ActivityCreateReport + ActivityReportList */
@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  private val viewModel: ViewModelMain by viewModels()
  private lateinit var viewBinding : ActivityMainBinding
  private lateinit var navDrawer: DrawerLayout
  private lateinit var progIndicator : ProgressBar
  private lateinit var sorryMsgTextView : TextView
  private lateinit var precautionRecyclerView : RecyclerView
  private lateinit var precautionAdapter : PrecautionAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    EspressoIdlingResource.increment() // ViewModel completion block handles the decrement since it runs even on error
    setUpToolbarAndNavView()
    setUpErrorMessaging()
    setUpPrecautionRV()
  }

  private fun setUpToolbarAndNavView() {
    // Since MainLauncher Activity label gives the app icon AND the toolbar its title, we purposely remove it here
    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_menu, " ") // Via title = " "

    navDrawer = viewBinding.myNavDrawer

    viewBinding.navView.apply {
      setNavigationItemSelectedListener(NavDrawerItemSelectedListener(::createNavDrawerIntent, ::finalizeNavDrawerItemSelection))
      getHeaderView(0).findViewById<Button>(R.id.navCloseButton).setOnClickListener { navDrawer.closeDrawers() }
    }

    progIndicator = viewBinding.progressIndicatorLayout.appProgressbar
    viewModel.isLoading.observe(this) { loading -> progIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE }
  }
  private fun setUpErrorMessaging() { // Sets up sorryTextView and Snackbar to display useful error messaging
    sorryMsgTextView = viewBinding.sorryTextView
    viewModel.toastMessage.observe(this) { message ->
      // This will ONLY ever receive a value if the precautionState liveData fails!
      // SO NO POINT observing the message from precautionState, it couldn't ever receive it due to the flow crashing
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        val listEmpty = viewModel.precautionState.value?.second?.isEmpty() ?: true
        with(sorryMsgTextView) {
          visibility = if (listEmpty) View.VISIBLE else View.INVISIBLE
          text = message
        }
        ShowSnackbar(viewBinding.myCoordinatorLayout, message, Snackbar.LENGTH_SHORT)
      }
    }
  }
  private fun setUpPrecautionRV() {
    precautionRecyclerView = viewBinding.precautionRV.apply {
      setHasFixedSize(true)
      precautionAdapter = PrecautionAdapter { _, healthPractice ->
        // Click Listener that creates an intent and launches the CreateReport Activity
        Intent(applicationContext, ActivityCreateReport::class.java).apply {
          putExtra(createReportPracticeExtra, healthPractice.name)
        }.also { createReportActivityLauncher.launch(it) }
      }
      adapter = precautionAdapter
    }

    viewModel.precautionState.observe(this) { (loading, newList) ->
      precautionAdapter.submitList(newList)
      val listEmpty = newList.isEmpty()
      with(sorryMsgTextView) {
        visibility = if (listEmpty) View.VISIBLE else View.INVISIBLE
        text = when {
          loading -> "Looking up precautions"
          listEmpty -> "Weird! Seems we don't have any available precautions to choose from!"
          else -> "Please try again later!"
        }
      }
    }
  }

  /* Next 2 funs = Menu Setup */
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.action_buttons, menu)
    return true // super.onCreateOptionsMenu(menu) // If returns false, menu won't display
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean { // Return true to end any further menu processing
    return when (item.itemId) {
      android.R.id.home -> { navDrawer.openDrawer(GravityCompat.START); true }
      R.id.action_settings -> {
        Intent(this, ActivitySettings::class.java).also { startActivity(it) }
        true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  // Following handles navigation from NavDrawer
  private val createReportActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      startActivity(Intent(applicationContext, ActivityReportList::class.java))
    }
  }

  private fun createNavDrawerIntent() = Intent(applicationContext, ActivityReportList::class.java)
  private fun finalizeNavDrawerItemSelection(intent: Intent) {
    navDrawer.closeDrawers()
    val precautions = viewModel.precautionState.value?.second ?: emptyList() // Get current value or empty if not set yet
    // Add each as separate arrays to intent so sortFilterActivity can dynamically create filter options
    addPrecautionsAndHealthPractices(intent, precautions)
    startActivity(intent)
  }
  private fun addPrecautionsAndHealthPractices(reportListIntent: Intent, precautionList: List<Precaution>) {
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
  }
}