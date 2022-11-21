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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.fragment.app.replace
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
  lateinit var toolbar: Toolbar
  private lateinit var coordinatorLayout: CoordinatorLayout
  private lateinit var navDrawer: DrawerLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)
    coordinatorLayout = viewBinding.mainCoordinatorLayout

    EspressoIdlingResource.increment() // ViewModel completion block handles the decrement since it runs even on error

    // Since MainLauncher Activity label gives the app icon AND the toolbar its title, so need to remove it here
    toolbar = viewBinding.toolbarLayout.homeToolbar.apply {
      title = ""
      setTitleTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorLight))
      setSupportActionBar(this)
    }

    setupNavView()

    setupFragmentListener()
  }

  private fun setupNavView() {
    navDrawer = viewBinding.navDrawer
    viewBinding.navView.apply {
      setNavigationItemSelectedListener(NavDrawerItemSelectedListener(::finalizeNavDrawerItemSelection))
      getHeaderView(0).findViewById<Button>(R.id.navCloseButton)
        .setOnClickListener { navDrawer.closeDrawers() }
    }
  }
  // Following handles navigation from NavDrawer + fills intent with current list of precaution types
  private fun finalizeNavDrawerItemSelection(bundle: Bundle) {
    navDrawer.closeDrawers()
    // Add the names lists to bundle separately so sortFilterActivity can dynamically create filter options
    val (precautionNames, healthPracticeNames) = viewModel.getNamesLists()
    bundle.putStringArrayList(PrecautionListExtra, precautionNames)
    bundle.putStringArrayList(HealthPracticeListExtra, healthPracticeNames)
    supportFragmentManager.commit { // Handle fragment transaction from fragmentMain to fragmentReportList
      setReorderingAllowed(true) // Optimize state changes as the fragment change gets animated

      // Could label source fragment for back stack if needed to jump straight back to it rather than require multiple pops back
      addToBackStack(null) // Needed regardless if adding or replacing UNLESS source fragment can be skipped when popping back

      // Replace() removes/destroys current Fragment instance then adds in layout frag container
      replace<FragmentReportList>(R.id.fragment_main_container, args = bundle) // Add() just stacks the view on top
    }
    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
  }

  private fun setupFragmentListener() {
    supportFragmentManager.setFragmentResultListener(ActionViewRequestKey, this, messagingListener)
    supportFragmentManager.setFragmentResultListener(KeyboardRequestKey, this, messagingListener)
    supportFragmentManager.setFragmentResultListener(NavDrawerRequestKey, this, messagingListener)
    supportFragmentManager.setFragmentResultListener(SnackbarRequestKey, this, messagingListener)
  }
  // Rather than open up coordinatorLayout and navDrawer to child fragments, handle their use from here!
  private val messagingListener = FragmentResultListener { requestKey, result ->
    when (requestKey) {
      ActionViewRequestKey -> { // If true value found then collapse toolbar action view
        if (result.getBoolean(ActionViewIsClosingParcel)) { toolbar.collapseActionView() }
      }
      KeyboardRequestKey -> {
        if (result.getBoolean(KeyboardIsClosingParcel)) { // If true value found then hide keyboard
          (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(viewBinding.root.windowToken, 0)
        }
      }
      NavDrawerRequestKey -> {
        if (result.getBoolean(NavDrawerIsOpeningParcel)) navDrawer.openDrawer(GravityCompat.START)
        else navDrawer.closeDrawer(GravityCompat.START)
      }
      SnackbarRequestKey -> {
        result.getString(SnackbarMessageParcel)?.let { ShowSnackbar(coordinatorLayout, it, Snackbar.LENGTH_SHORT) }
      }
    }
  }
}