package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityCreateReportBinding
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelCreateReport
import java.time.LocalTime

/* Activity to File a New Health Report */
@AndroidEntryPoint
class ActivityCreateReport : AppCompatActivity() {
  private val viewModel: ViewModelCreateReport by viewModels()
  private lateinit var viewBinding : ActivityCreateReportBinding
  private lateinit var progressIndicator : ProgressBar // If never init'd then will crash!
  private lateinit var headerTV : TextView
  private lateinit var dateET : EditText
  private lateinit var dateTimeSetListener: ListenerDateTimeSet
  private lateinit var employeeSpinner : Spinner
  private lateinit var healthPracticeSpinner: Spinner
  private lateinit var locationSpinner : Spinner
  private val spinnerListener = SpinnerItemSelectionListener()
  private lateinit var createReportButton : Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    with(window) {
      requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
      enterTransition = Slide(Gravity.RIGHT)
      exitTransition = Slide(Gravity.LEFT)
    }
    viewBinding = ActivityCreateReportBinding.inflate(layoutInflater)
    setContentView(viewBinding.root) // apply may work above for quick setContentView setup

    // Following = how to manage <include/> layouts - Give include an id then search inside of it!
    SetupToolbar(this, viewBinding.toolbarLayout.homeToolbar, R.drawable.ic_back_arrow)

    setupProgressIndicator()
    setupHeaderTextView()
    setupDateEditText()
    setupSpinners()

    createReportButton = viewBinding.createReportButton.apply { setOnClickListener(SubmitReportClickListener()) }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean { // Return true to end any further menu processing
    return when (item.itemId) {
      android.R.id.home -> { // Could also override onBackPressed but better not to (i.e. app closes if no backStack + backButton)
        // If at this activity w/out a backStack then launch mainActivity, finish this one
        if (this.isTaskRoot) { startActivity(Intent(this, ActivityMain::class.java)) }
        finishAfterTransition() // If there's a backstack!, should behave as normal (onBackPressed() to mainActivity)
        true // Finally always return true (done working with menu items here)
      }
      else -> super.onOptionsItemSelected(item) // Defaults to false (meaning nothing happens)
    }
  }

  private fun setupProgressIndicator() {
    progressIndicator = viewBinding.progressIndicator.appProgressbar
    viewModel.loadingState.observe(this) { loading -> progressIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE }
  }
  private fun setupHeaderTextView() {
    headerTV = viewBinding.headerTV // Should default to "New Observation" and will be changed when observer
    viewModel.healthPracticeHeaderText.observe(this) { headerTV.text = it } // emits a string from flow map func
  }
  private fun setupDateEditText() {
    // Kotlin can use "apply" (and other scope funcs) as convenient extension funcs that can often times help in setting up
    // the vars using these scope funcs. Here "apply" acts like a Builder, setting props before returning the fully setup instance
    dateET = viewBinding.dateEditText.apply {
      showSoftInputOnFocus = false; requestFocus()
      dateTimeSetListener = ListenerDateTimeSet(viewModel::updateDate) // Save time by not reSetting every dateET click
      setOnClickListener { with(LocalTime.now()) {
        TimePickerDialog(this@ActivityCreateReport, dateTimeSetListener, hour, minute, false).show()
      }}
    }
    viewModel.dateTimeString.observe(this) { dateET.setText(it) } // MUST use setText to use strings in editTexts
  }

  // Spinners Setup in Order of Appearance in layout file
  private fun setupSpinners() {
    val selectedPracticeType = intent.getStringExtra(CreateReportPracticeExtra) ?: ""
    EspressoIdlingResource.increment()
    employeeSpinner = viewBinding.employeeSpinner.apply { onItemSelectedListener = spinnerListener }
    healthPracticeSpinner = viewBinding.healthPracticeSpinner.apply { onItemSelectedListener = spinnerListener }
    locationSpinner = viewBinding.facilitySpinner.apply { onItemSelectedListener = spinnerListener }
    // Next starts loading spinner data, 1st w/ an emptyList then the full list from the server
    viewModel.adapterData.observe(this) { (employeeList, healthPracticeList, locationList) ->
      // COULD keep refs to each arrayAdapter & mutate their lists via clear() then addAll(newList)
      // BUT making new adapters w/ each new list emitted and reSetting the spinners works just as well!
      ArrayAdapter(this, R.layout.custom_spinner_dropdown, employeeList).also { employeeSpinner.adapter = it }
      ArrayAdapter(this, R.layout.custom_spinner_dropdown, locationList).also { locationSpinner.adapter = it }
      ArrayAdapter(this, R.layout.custom_spinner_dropdown, healthPracticeList).also {
        healthPracticeSpinner.adapter = it
        if (healthPracticeList.isEmpty()) { return@also } // If empty, DON'T select a healthPractice
        val selectedIndex = viewModel.selectedHealthPracticeIndex(healthPracticeList, selectedPracticeType)
        if (selectedIndex != -1) { healthPracticeSpinner.setSelection(selectedIndex) } // MUST prevent outOfRange error
      }
    }
  }
  private inner class SpinnerItemSelectionListener : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
      when (parent?.id) {
        R.id.healthPracticeSpinner -> Log.d("Profession Spinner", "Nothing selected")
        R.id.employeeSpinner -> Log.d("Employee spinner", "Nothing selected")
        R.id.facilitySpinner -> Log.d("Facility Spinner", "Nothing selected")
//        R.id.unitSpinner -> Log.d("Unit Spinner", "Not selected")
//        R.id.roomSpinner -> Log.d("Room Spinner", "Not selected")
        else -> Log.d("Nothing selected", "No spinner selected")
      }
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      when (parent?.id) {
        R.id.healthPracticeSpinner -> viewModel.updateHealthPractice(parent.selectedItem as? HealthPractice)
        R.id.employeeSpinner -> viewModel.updateReport(employee = parent.selectedItem as? Employee)
        R.id.facilitySpinner -> viewModel.updateReport(location = parent.selectedItem as? Location)
//        R.id.unitSpinner -> Log.d("Unit Spinner", "Selected")
//        R.id.roomSpinner -> Log.d("Room Spinner", "Selected")
        else -> Log.w("ItemSelection When", "Else statement ran for some reason!")
      }
    }
  }

  private inner class SubmitReportClickListener : View.OnClickListener {
    override fun onClick(clickedView: View?) {
      if (viewModel.dateTimeString.value.isNullOrBlank()) { // Indicates date was likely not selected
        AlertDialog.Builder(this@ActivityCreateReport).run {
          setPositiveButton(R.string.alert_dialog_ok) { _, _ -> completeReportSubmission() }
          setNegativeButton(R.string.alert_dialog_cancel) { _, _ ->
            ShowSnackbar(viewBinding.myCoordinatorLayout, resources.getString(R.string.missing_date_hint), Snackbar.LENGTH_SHORT)
          }
          setMessage(R.string.date_alert_dialog_message)
          setTitle(R.string.date_alert_dialog_title)
          create() // After set up return the alert dialog via create
        }.show()
      }
      else { completeReportSubmission() }
    }
  }
  // Call this next fun either after user presses OK in alertDialog to use current time
  // OR after hitting submit because user correctly filled out all input
  private fun completeReportSubmission() {
    setResult(Activity.RESULT_OK)
    viewModel.submitReport()
    // This conditional handles nav when using shortcut: Start main, since shortcut didn't, go to reportList, don't close app
    if (this.isTaskRoot) { startActivity(Intent(this, ActivityMain::class.java)) }
    finish()
  }
}