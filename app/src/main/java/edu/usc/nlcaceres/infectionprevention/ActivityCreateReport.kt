package edu.usc.nlcaceres.infectionprevention

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import edu.usc.nlcaceres.infectionprevention.helpers.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityCreateReport : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

  private lateinit var progressIndicator : ProgressBar // If never init'd then will crash!

  private lateinit var headerTV : TextView

  private lateinit var dateET : EditText
  private var selectedDate : Date? = null

  private lateinit var locationSpinner : Spinner
  private val locationList : ArrayList<Location> = arrayListOf()
  private var selectedLocation : Location? = null

  private lateinit var healthPracticeSpinner: Spinner
  private val healthPracticeList : ArrayList<HealthPractice> = arrayListOf()
  private var selectedPractice : HealthPractice? = null
  private var selectedPracticeName : String? = null

  private lateinit var employeeSpinner: Spinner
  private val employeeList : ArrayList<Employee> = arrayListOf()
  private var selectedEmployee : Employee? = null

  private val spinnerListener = SpinnerItemSelectionListener()

  private lateinit var createReportButton : Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_report)
    val createReportToolbar = findViewById<Toolbar>(R.id.home_toolbar)
    setSupportActionBar(createReportToolbar)
    supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    //getSupportFragmentManager().beginTransaction().replace(R.id.create_report_fragment, CreateReportFragment.newInstance()).commit();
    // transaction.addToBackStack(null); // Important for adding to back stack while keeping it one activity

    progressIndicator = findViewById(R.id.app_progressbar)
    progressIndicator.visibility = View.VISIBLE

    selectedPracticeName = intent?.getStringExtra(createReportPracticeExtra)
    val headingText = "New $selectedPracticeName Observation"
    headerTV = findViewById<TextView>(R.id.headerTV).apply { text = headingText }
    healthPracticeSpinner = findViewById<Spinner>(R.id.healthPracticeSpinner).also { fetchHealthPractices() }

    dateET = findViewById<EditText>(R.id.dateEditText).apply { // Builder pattern takes it and return it all set up! (in case this keyword is needed)
      showSoftInputOnFocus = false; requestFocus()
      setOnClickListener {
        val c = Calendar.getInstance()
        TimePickerDialog(this@ActivityCreateReport, this@ActivityCreateReport, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
      }
    }

    employeeSpinner = findViewById<Spinner>(R.id.employeeSpinner).also { fetchEmployees() }
    locationSpinner = findViewById<Spinner>(R.id.facilitySpinner).also { fetchLocations() }

    createReportButton = findViewById<Button>(R.id.createReportButton).apply { setOnClickListener(SubmitReportClickListener()) }
  }

  private fun fetchEmployees() { // ArrayList Serializer Request example
    val employeesListRequest = StringRequest(employeesURL, Response.Listener<String> {
      try {
        employeeList.addAll(underscoredNameGson().fromJson(it, TypeToken.getParameterized(ArrayList::class.java, Employee::class.java).type))
      } catch (err : Error) { Log.w("Employee parse err", err.localizedMessage ?: err.toString())}
      ArrayAdapter<Employee>(this, R.layout.custom_spinner_dropdown, employeeList).also { adapter ->
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Take this away and dropdown items will use the selected item layout
        employeeSpinner.adapter = adapter
        employeeSpinner.onItemSelectedListener = spinnerListener
      }
      hideProgIndicator()
    }, Response.ErrorListener { Log.w("Employee fetch err", it.localizedMessage ?: it.toString()) })

    employeesListRequest.tag = CreateReportFragCancelTag
    employeesListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(employeesListRequest)
  }

  private fun fetchHealthPractices() {
    val practicesListRequest = JsonArrayRequest(practicesURL, Response.Listener {
      try {
        healthPracticeList.addAll(GsonBuilder().registerTypeAdapter(HealthPractice::class.java, HealthPracticeDeserializer()).create().
            fromJson(it.toString(), TypeToken.getParameterized(ArrayList::class.java, HealthPractice::class.java).type))
      } catch (err : Error) { Log.w("Prof Response Err", err.localizedMessage ?: err.toString())}
      val selectedPracticeIndex = healthPracticeList.indexOfFirst { healthPractice -> healthPractice.name == selectedPracticeName }
      ArrayAdapter<HealthPractice>(this, R.layout.custom_spinner_dropdown, healthPracticeList).also { adapter ->
        healthPracticeSpinner.adapter = adapter
        healthPracticeSpinner.setSelection(selectedPracticeIndex)
        healthPracticeSpinner.onItemSelectedListener = spinnerListener
      }
      hideProgIndicator()
    }, Response.ErrorListener { Log.w("Profession Fetch error", it.localizedMessage ?: it.toString()) })

    practicesListRequest.tag = CreateReportFragCancelTag
    practicesListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(practicesListRequest)
  }

  private fun fetchLocations() { // Object Serializer Request example
    val locationListRequest = JsonArrayRequest(locationsURL,
        Response.Listener<JSONArray> { // Could also use object expression (e.g. object : Response.Listener<JSONArray> {}). SAM conversions work w/ Java interfaces, not abstract classes
          for (i in 0 until it.length()) { // Kotlin equiv of standard Java for loop, (due to JSONArray not being iterable)
            try { locationList.add(Gson().fromJson(it.getJSONObject(i).toString(), Location::class.java)) }
            catch (err: Error) { Log.w("Location Response Err", err.localizedMessage ?: err.toString()) }
          }
          ArrayAdapter<Location>(this, R.layout.custom_spinner_dropdown, locationList).also { adapter ->
            locationSpinner.adapter = adapter
            locationSpinner.onItemSelectedListener = spinnerListener
          }
          hideProgIndicator()
        }, Response.ErrorListener { // Both here are SAM conversions, working identically to object expression pattern of kotlin
      error -> Log.w("Location Fetch Error", error.localizedMessage ?: error.toString()) // Also can name param and use arrow lambda instead of it keyword!
    }) // If this was our own kotlin code/fun we could inline it for better performance with SAMCs but since java and not ours, not possible
    locationListRequest.tag = CreateReportFragCancelTag
    locationListRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(locationListRequest)
  }

  private fun hideProgIndicator() {
    if (healthPracticeList.isNotEmpty() && locationList.isNotEmpty() && employeeList.isNotEmpty()) { progressIndicator.visibility = View.INVISIBLE }
  }

  private inner class SpinnerItemSelectionListener : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
      when (parent?.id) {
        R.id.facilitySpinner -> Log.d("Facility Spinner", "Nothing selected")
//        R.id.unitSpinner -> Log.d("Unit Spinner", "Not selected")
//        R.id.roomSpinner -> Log.d("Room Spinner", "Not selected")
        R.id.healthPracticeSpinner -> Log.d("Profession Spinner", "Nothing selected")
        R.id.employeeSpinner -> Log.d("Employee spinner", "Nothing selected")
        else -> Log.d("Nothing selected", "No spinner selected")
      }
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      when (parent?.id) {
        R.id.facilitySpinner -> selectedLocation = parent.selectedItem as? Location
//        R.id.unitSpinner -> Log.d("Unit Spinner", "Selected")
//        R.id.roomSpinner -> Log.d("Room Spinner", "Selected")
        R.id.healthPracticeSpinner -> { selectedPractice = parent.selectedItem as? HealthPractice
          val headerTVText = "New ${selectedPractice?.name} Observation"
          headerTV.text = headerTVText }
        R.id.employeeSpinner -> selectedEmployee = parent.selectedItem as? Employee
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
            Toast.makeText(context, "Tap the Time & Date text box above to set them up!", Toast.LENGTH_SHORT).apply {
              ((view as LinearLayout).getChildAt(0) as TextView).textSize = 20.0f // Grab toast's view, then textView in it and update textSize
              show() } // then show the toast
          }
          setMessage(R.string.date_alert_dialog_message)
          setTitle(R.string.date_alert_dialog_title)
          create() // After set up return the alert dialog via create
        }.show()
      }
    }
  }

  private fun completeReportSubmission() {
    val newReport = if (selectedDate != null) Report(null, selectedEmployee, selectedPractice, selectedLocation, Date())
    else Report(null, selectedEmployee, selectedPractice, selectedLocation, selectedDate) // Dialog OK triggers Date() vs selectedDate usage

    val jsonReport = Gson().toJson(newReport) // Serialized Name change for date is key to proper json format
    val reportJsonRequest = JsonObjectRequest(Request.Method.POST, reportCreationURL, JSONObject(jsonReport),
        Response.Listener { Log.d("New Report Success", "Successfully sent new report")},
        Response.ErrorListener { Log.w("New Report Err", it.localizedMessage ?: it.toString()) })
    reportJsonRequest.tag = CreateReportFragCancelTag
    reportJsonRequest.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

//    RequestQueueSingleton.getInstance(context!!.applicationContext).addToRequestQueue(reportJsonRequest)
    setResult(Activity.RESULT_OK)
    finish()
  }

  override fun onStop() {
    super.onStop()
    RequestQueueSingleton.getInstance(applicationContext).requestQueue.cancelAll(CreateReportFragCancelTag)
  }

  override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
    val AM_PM = if (hour < 12) "AM" else "PM" // Always seems to return 24 hour time so to check if AM or PM
    val hourOfDay = if (hour > 12) hour - 12 else hour // Prevent military time
    val timeOfDay = String.format("%d:%02d", hourOfDay, minute) // Format to have two zeros for minute

    val dateStr = "$timeOfDay $AM_PM" // Don't concatenate in setText
    dateET.setText(dateStr)

    val c = Calendar.getInstance()
    DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
  }

  override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
    val fullDateTimeStr = "${dateET.text} $month/$day/$year"
    val dateFormat = SimpleDateFormat("h:mm a MM/dd/yy", Locale.getDefault())
    selectedDate = dateFormat.parse(fullDateTimeStr) // Returns a date obj

//    val calendar = Calendar.getInstance().apply { set(year, month, day) }
////    val fullDate = dateFormat.format(calendar.time)
    dateET.setText(fullDateTimeStr)
  }
}