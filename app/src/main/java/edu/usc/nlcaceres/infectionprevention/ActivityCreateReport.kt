package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.app.DatePickerDialog
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
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import edu.usc.nlcaceres.infectionprevention.databinding.ActivityCreateReportBinding
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.util.*
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import kotlin.collections.ArrayList

/* Activity to File a New Health Report */
class ActivityCreateReport : AppCompatActivity() {

  private lateinit var viewBinding : ActivityCreateReportBinding

  private lateinit var progressIndicator : ProgressBar // If never init'd then will crash!

  private lateinit var headerTV : TextView

  private lateinit var dateET : EditText
  private var selectedDate : Instant? = null
  private val dateTimeSetListener = DateTimeSetListener() // Save time recreating w/ every dateET click

  private lateinit var employeeSpinner : Spinner
  private val employeeList : ArrayList<Employee> = arrayListOf()
  private var selectedEmployee : Employee? = null
  private lateinit var healthPracticeSpinner: Spinner
  private val healthPracticeList : ArrayList<HealthPractice> = arrayListOf()
  private var selectedPractice : HealthPractice? = null
  private var selectedPracticeName : String? = null
  private lateinit var locationSpinner : Spinner
  private val locationList : ArrayList<Location> = arrayListOf()
  private var selectedLocation : Location? = null
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

    //getSupportFragmentManager().beginTransaction().replace(R.id.create_report_fragment, CreateReportFragment.newInstance()).commit();
    // transaction.addToBackStack(null); // Important for adding to back stack while keeping it one activity

    progressIndicator = viewBinding.progressIndicator.appProgressbar.apply { visibility = View.VISIBLE }

    selectedPracticeName = intent.getStringExtra(CreateReportPracticeExtra)
    headerTV = viewBinding.headerTV.apply { // Handle brief "New null Observation" b4 spinner loads to set TV to "New PPE..."
      text = if (selectedPracticeName != null) "New $selectedPracticeName Observation" else "New Observation"
    }

    // Instead of a true Builder pattern, Kotlin can use apply to set the var's props, return the instance, & init the var
    dateET = viewBinding.dateEditText.apply {
      showSoftInputOnFocus = false; requestFocus()
      setOnClickListener { // Use Calendar instance to setup timePickerDialog and show it
        Calendar.getInstance().also { TimePickerDialog(this@ActivityCreateReport, dateTimeSetListener,
          it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE), false).show() }
      }
    }

    employeeSpinner = viewBinding.employeeSpinner.also { setUpEmployeeSpinner() }
    healthPracticeSpinner = viewBinding.healthPracticeSpinner.also { setUpHealthPracticeSpinner() }
    locationSpinner = viewBinding.facilitySpinner.also { setUpLocationSpinner() }

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

  private inner class DateTimeSetListener : DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    // Handles Both TimePicker and DatePicker SetListeners
    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
      val amOrPM = if (hour < 12) "AM" else "PM" // Always seems to return 24 hour time so to check if AM or PM
      val hourOfDay = when { // Need to handle military time
        hour > 12 -> hour - 12 // 13, 14, etc down to 1, 2, etc.
        hour == 0 -> 12 // Midnight
        else -> hour
      }
      val timeOfDay = String.format("%d:%02d", hourOfDay, minute) // Format to 12:00, 12:05 (NOT 12:5), 12:10 (NOT 12:1)

      val dateStr = "$timeOfDay $amOrPM" // Don't concatenate in setText below
      dateET.setText(dateStr)

      Calendar.getInstance().also { c -> // 'this' will reuse the listener currently calling onTimeSet
        DatePickerDialog(this@ActivityCreateReport, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
      }
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) { // Month is 0 indexed, Day is not.
      // Instead of initing a Calendar obj w/ year, month & date then using SimpleDateFormat.format() to make the string
      val fullTimeDateStr = "${dateET.text} ${month+1}/$day/$year" // Straight to a string! (+1 for month to be correct number)
      dateET.setText(fullTimeDateStr)
      // Need single 'd' and 'M' in pattern since it is never 0-padded aka 12/01/1996 vs 12/1/1996
      println("ZoneID = ${ZoneId.of(TimeZone.getDefault().id)}")
      val dateFormatter = DateTimeFormatter.ofPattern("h:mm a M/d/yyyy")
        .withZone(ZoneId.of(TimeZone.getDefault().id))
      selectedDate = ZonedDateTime.parse(fullTimeDateStr, dateFormatter).toInstant()
    }
  }

  // Spinners Setup in Order of Appearance in layout file
  private fun setUpEmployeeSpinner() { // ArrayList<Employee> Serializer Request example
    EspressoIdlingResource.increment()
    val employeesListRequest = StringRequest(employeesURL, { // Response.Listener<String> Single Abstract Method Interface (SAM)
      try { // Receive a Json String to convert into Gson and serialize to a list of Employees
        employeeList.addAll(snakeCaseGson().fromJson(it, TypeToken.getParameterized(ArrayList::class.java, Employee::class.java).type))
      }
      catch (err : Error) { Log.w("Employee parse err", err.localizedMessage ?: err.toString()) }

      ArrayAdapter(this, R.layout.custom_spinner_dropdown, employeeList).also { adapter ->
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // W/out this, dropdown items use custom_spinner layout
        employeeSpinner.adapter = adapter
        employeeSpinner.onItemSelectedListener = spinnerListener
      }
      HideProgressIndicator(healthPracticeList.isNotEmpty() && locationList.isNotEmpty() && employeeList.isNotEmpty(), progressIndicator)
      EspressoIdlingResource.decrement()
    }, { Log.w("Employee fetch err", it.localizedMessage ?: it.toString()); EspressoIdlingResource.decrement() }) // Response.ErrorListener SAM

    employeesListRequest.tag = CreateReportFragCancelTag
    employeesListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(employeesListRequest)
  }
  private fun setUpHealthPracticeSpinner() {
    EspressoIdlingResource.increment()
    val practicesListRequest = JsonArrayRequest(practicesURL, { // Response.Listener<JSONArray> SAM
      try {
        healthPracticeList.addAll(GsonBuilder().registerTypeAdapter(HealthPractice::class.java, HealthPracticeDeserializer()).create().
          fromJson(it.toString(), TypeToken.getParameterized(ArrayList::class.java, HealthPractice::class.java).type))
      }
      catch (err : Error) { Log.w("Prof Response Err", err.localizedMessage ?: err.toString())}

      val selectedPracticeIndex = healthPracticeList.indexOfFirst { healthPractice -> healthPractice.name == selectedPracticeName }
      ArrayAdapter(this, R.layout.custom_spinner_dropdown, healthPracticeList).also { arrAdapter ->
        healthPracticeSpinner.adapter = arrAdapter
        healthPracticeSpinner.setSelection(selectedPracticeIndex)
        healthPracticeSpinner.onItemSelectedListener = spinnerListener
      }
      HideProgressIndicator(healthPracticeList.isNotEmpty() && locationList.isNotEmpty() && employeeList.isNotEmpty(), progressIndicator)
      EspressoIdlingResource.decrement()
      // Both here are SAM conversions, working identically to object expression pattern of kotlin... See fetchLocations()
    }, { Log.w("Profession Fetch error", it.localizedMessage ?: it.toString()); EspressoIdlingResource.decrement() })

    practicesListRequest.tag = CreateReportFragCancelTag
    practicesListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(practicesListRequest)
  }
  private fun setUpLocationSpinner() { // Object Serializer Request example
    EspressoIdlingResource.increment()
    val locationListRequest = JsonArrayRequest(locationsURL,
      { // The object expression (ex: "object : Response.Listener<JSONArray> {}"). SAM conversions work w/ Java interfaces, not abstract classes
          for (i in 0 until it.length()) { // Kotlin equiv of standard Java for loop, (due to JSONArray not being iterable)
            try { locationList.add(Gson().fromJson(it.getJSONObject(i).toString(), Location::class.java)) }
            catch (err: Error) { Log.w("Location Response Err", err.localizedMessage ?: err.toString()) }
          }
          ArrayAdapter(this, R.layout.custom_spinner_dropdown, locationList).also { adapter ->
            locationSpinner.adapter = adapter
            locationSpinner.onItemSelectedListener = spinnerListener
          }
          HideProgressIndicator(healthPracticeList.isNotEmpty() && locationList.isNotEmpty() && employeeList.isNotEmpty(), progressIndicator)
        EspressoIdlingResource.decrement()
      }, { // Also can name param and use arrow lambda instead of it keyword!
        error -> Log.w("Location Fetch Error", error.localizedMessage ?: error.toString())
        EspressoIdlingResource.decrement()
      }) // If this was our own kotlin code/fun we could inline it for better performance with SAMCs but since java and not ours, not possible
    locationListRequest.tag = CreateReportFragCancelTag
    locationListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(locationListRequest)
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
        R.id.healthPracticeSpinner -> {
          selectedPractice = parent.selectedItem as? HealthPractice
          selectedPracticeName = selectedPractice?.name
          headerTV.text = if (selectedPracticeName != null) "New $selectedPracticeName Observation"
            else "New Observation"
        }
        R.id.employeeSpinner -> selectedEmployee = parent.selectedItem as? Employee
        R.id.facilitySpinner -> selectedLocation = parent.selectedItem as? Location
//        R.id.unitSpinner -> Log.d("Unit Spinner", "Selected")
//        R.id.roomSpinner -> Log.d("Room Spinner", "Selected")
        else -> Log.w("ItemSelection When", "Else statement ran for some reason!")
      }
    }
  }

  private inner class SubmitReportClickListener : View.OnClickListener {
    override fun onClick(clickedView: View?) {
      if (selectedDate == null) {
        AlertDialog.Builder(this@ActivityCreateReport).run {
          setPositiveButton(R.string.alert_dialog_ok) { _, _ -> completeReportSubmission() }
          setNegativeButton(R.string.alert_dialog_cancel) { _, _ ->
            // Settings icon to SettingsPrefs Activity
            Snackbar.make(viewBinding.myCoordinatorLayout,"Tap the Time & Date text box above to set them up!", Snackbar.LENGTH_SHORT).show()
          }
          setMessage(R.string.date_alert_dialog_message)
          setTitle(R.string.date_alert_dialog_title)
          create() // After set up return the alert dialog via create
        }.show()
      }
      else { completeReportSubmission() }
    }
  }

  private fun completeReportSubmission() {
    // Dialog OK triggers Date() vs selectedDate usage
    val newReport = Report(null, selectedEmployee, selectedPractice, selectedLocation, selectedDate ?: Instant.now())

    JsonObjectRequest(Request.Method.POST, reportCreationURL, JSONObject(Gson().toJson(newReport)),
      { Log.d("New Report Success", "Successfully sent new report") },
      { Log.w("New Report Err", it.localizedMessage ?: it.toString()) }).
    let {
      it.tag = CreateReportFragCancelTag
      it.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
//      RequestQueueSingleton.getInstance(context!!.applicationContext).addToRequestQueue(reportJsonRequest)
    }

    setResult(Activity.RESULT_OK)
    // Following conditional considers nav when using shortcut: Go to main then reportList, don't close app.
    if (this.isTaskRoot) { startActivity(Intent(this, ActivityMain::class.java)) }
    finish()
  }

  override fun onStop() {
    super.onStop()
    RequestQueueSingleton.getInstance(applicationContext).requestQueue.cancelAll(CreateReportFragCancelTag)
  }
}