package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityMainBinding
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

/* Homepage that allows users to choose a type of health violation that occurred
 and create a report for it
 Links to: FragmentCreateReport + FragmentReportList */
@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  private val viewModel: ViewModelMain by viewModels() // Still requires Hilt to retrieve the viewModel
  private lateinit var viewBinding : ActivityMainBinding
  private lateinit var coordinatorLayout: CoordinatorLayout
  private lateinit var toolbar: Toolbar
  private lateinit var appBarConfig: AppBarConfiguration

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)
    coordinatorLayout = viewBinding.mainCoordinatorLayout

    EspressoIdlingResource.increment() // ViewModel completion block handles the decrement since it runs even on error

    val navController = setupActionBar()
    setupNavDrawer(navController)
  }

  private fun setupActionBar(): NavController {
    toolbar = viewBinding.toolbarLayout.homeToolbar.apply { // Let NavGraph fragment element's label set Title
      setTitleTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorLight)) // Just add color
      setSupportActionBar(this) // Establish this as the app-wide toolbar
    }
    // Then link the actionBar to the navGraph via the navController & appBarConfig
    val navHost = supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
    val navController = navHost.navController

    appBarConfig = AppBarConfiguration(navController.graph, viewBinding.navDrawer)

    setupActionBarWithNavController(navController, appBarConfig) // Allow NavComponent to control Home/Up Buttons

    return navController
  }

  // Following allows NavHostFragment to hijack the Actionbar Home/Up button & open/close the NavDrawer
  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.fragment_nav_host)
    return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
  }

  private fun setupNavDrawer(navController: NavController) {
    viewBinding.navView.apply {
      getHeaderView(0).findViewById<Button>(R.id.navCloseButton)
        .setOnClickListener { viewBinding.navDrawer.closeDrawers() }
      setupWithNavController(navController) // Links NavDrawer & its items to NavComponent/Graph
    }
    navController.addOnDestinationChangedListener { _, destination, args ->
      val upIndicator = when (destination.id) {
        R.id.createReportFragment, R.id.settingsFragment, R.id.reportListFragment -> R.drawable.ic_back_arrow
        R.id.sortFilterFragment -> R.drawable.ic_close
        else -> R.drawable.ic_menu
      } // To minimize pop-in, we update toolbar on Nav, not on Fragment CREATE lifecycle state
      supportActionBar?.setUpIndicator(upIndicator)

      // If using navDrawer item to go to destination, use "CloseDrawer" argument to closeDrawer b4 entering next fragment
      if (args?.getBoolean("CloseDrawer") == true) { viewBinding.navDrawer.closeDrawers() }
    }
  }

  // Replace Fragment Listeners w/ simple helper functions since modern Android usually only uses 1 Host Activity
  fun collapseActionView() = toolbar.collapseActionView()
  fun hideKeyboard() = (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(viewBinding.root.windowToken, 0)
  fun showSnackbar(message: String) {
    ShowSnackbar(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
  }
}