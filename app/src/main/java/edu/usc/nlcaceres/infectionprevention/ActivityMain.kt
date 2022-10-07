package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.app.ActivityOptions
import android.view.Window
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityMainBinding
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.SetupToolbar
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

/* Homepage that allows users to choose a type of health violation that occurred
 and create a report for it
 Links to: ActivityCreateReport + ActivityReportList */
@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  private val viewModel: ViewModelMain by viewModels() // Still requires Hilt to retrieve the viewModel
  private lateinit var viewBinding : ActivityMainBinding
  lateinit var coordinatorLayout: CoordinatorLayout
  private lateinit var navDrawer: DrawerLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    with(window) {
      requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS) // If did not set in styles.xml
      exitTransition = Slide(Gravity.LEFT)
    }
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)
    coordinatorLayout = viewBinding.mainCoordinatorLayout

    EspressoIdlingResource.increment() // ViewModel completion block handles the decrement since it runs even on error

    // Since MainLauncher Activity label gives the app icon AND the toolbar its title, we purposely remove it here
    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_menu, " ") // Via title = " "

    setupNavView()
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

  private fun setupNavView() {
    navDrawer = viewBinding.navDrawer
    viewBinding.navView.apply {
      setNavigationItemSelectedListener(NavDrawerItemSelectedListener(::createNavDrawerIntent, ::finalizeNavDrawerItemSelection))
      getHeaderView(0).findViewById<Button>(R.id.navCloseButton)
        .setOnClickListener { navDrawer.closeDrawers() }
    }
  }
  // Following handles navigation from NavDrawer + fills intent with current list of precaution types
  private fun createNavDrawerIntent() = Intent(applicationContext, ActivityReportList::class.java)
  private fun finalizeNavDrawerItemSelection(intent: Intent) {
    navDrawer.closeDrawers()
    val precautions = viewModel.precautionState.value?.second ?: emptyList() // Get current value or empty if not set yet
    // Add each as separate arrays to intent so sortFilterActivity can dynamically create filter options
    addPrecautionsAndHealthPractices(intent, precautions)
    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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