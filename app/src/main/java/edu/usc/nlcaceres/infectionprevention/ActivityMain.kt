package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.transition.Slide
import android.view.Window
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityMainBinding
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.healthPracticeListExtra
import edu.usc.nlcaceres.infectionprevention.util.precautionListExtra
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

/* Homepage that allows users to choose a type of health violation that occurred
 and create a report for it
 Links to: ActivityCreateReport + FragmentReportList */
@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  private val viewModel: ViewModelMain by viewModels() // Still requires Hilt to retrieve the viewModel
  private lateinit var viewBinding : ActivityMainBinding
  lateinit var toolbar: Toolbar
  lateinit var coordinatorLayout: CoordinatorLayout
  lateinit var navDrawer: DrawerLayout

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

    // Since MainLauncher Activity label gives the app icon AND the toolbar its title, so need to remove it here
    toolbar = viewBinding.toolbarLayout.homeToolbar.apply {
      title = ""
      setTitleTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorLight))
      setSupportActionBar(this)
    }

    setupNavView()
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
    bundle.putStringArrayList(precautionListExtra, precautionNames)
    bundle.putStringArrayList(healthPracticeListExtra, healthPracticeNames)
    supportFragmentManager.commit { // Handle fragment transaction from fragmentMain to fragmentReportList
      setReorderingAllowed(true) // Optimize state changes as the fragment change gets animated

      // Could label source fragment for back stack if needed to jump straight back to it rather than require multiple pops back
      addToBackStack(null) // Needed regardless if adding or replacing UNLESS source fragment can be skipped when popping back

      // Replace() removes/destroys current Fragment instance then adds in layout frag container
      replace<FragmentReportList>(R.id.fragment_main_container, args = bundle) // Add() just stacks the view on top
    }
    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
  }
}