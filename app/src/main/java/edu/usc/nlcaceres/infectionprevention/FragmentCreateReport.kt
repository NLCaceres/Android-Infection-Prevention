package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentCreateReportBinding
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelCreateReport
import java.time.LocalTime

/* Activity to File a New Health Report */
@AndroidEntryPoint
class FragmentCreateReport : Fragment(R.layout.fragment_create_report) {
  private val viewModel: ViewModelCreateReport by viewModels()
  private var _viewBinding: FragmentCreateReportBinding? = null
  private val viewBinding get() = _viewBinding!!
  private lateinit var progressIndicator : ProgressBar // lateinit vars must be init'd or will throw!
  private lateinit var headerTV : TextView
  private lateinit var dateET : EditText
  private lateinit var dateTimeSetListener: ListenerDateTimeSet
  private lateinit var employeeSpinner : Spinner
  private lateinit var healthPracticeSpinner: Spinner
  private lateinit var locationSpinner : Spinner
  private val spinnerListener = SpinnerItemSelectionListener()
  private lateinit var createReportButton : Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentCreateReportBinding.inflate(layoutInflater)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (activity as AppCompatActivity).supportActionBar?.setUpIndicator(R.drawable.ic_back_arrow)
    requireActivity().addMenuProvider(CreateReportMenu(), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setupProgressIndicator()
    setupHeaderTextView()
    setupDateEditText()
    setupSpinners()

    createReportButton = viewBinding.createReportButton.apply { setOnClickListener(SubmitReportClickListener()) }
  }

  private inner class CreateReportMenu: MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) { }
    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
      android.R.id.home -> { parentFragmentManager.popBackStack(); true }
      else -> false
    }
  }

  private fun setupProgressIndicator() {
    progressIndicator = viewBinding.progressIndicator.appProgressbar
    viewModel.loadingState.observe(viewLifecycleOwner) { loading ->
      progressIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
  }
  private fun setupHeaderTextView() {
    headerTV = viewBinding.headerTV // Should default to "New Observation" and will be changed when observer
    viewModel.healthPracticeHeaderText.observe(viewLifecycleOwner) { headerTV.text = it } // emits a string from flow map func
  }
  private fun setupDateEditText() {
    // Kotlin can use "apply" (and other scope funcs) as convenient extension funcs that can often times help in setting up
    // the vars using these scope funcs. Here "apply" acts like a Builder, setting props before returning the fully setup instance
    dateET = viewBinding.dateEditText.apply {
      showSoftInputOnFocus = false; requestFocus()
      dateTimeSetListener = ListenerDateTimeSet(viewModel::updateDate) // Save time by not reSetting every dateET click
      setOnClickListener { with(LocalTime.now()) {
        TimePickerDialog(requireContext(), dateTimeSetListener, hour, minute, false).show()
      }}
    }
    viewModel.dateTimeString.observe(viewLifecycleOwner) { dateET.setText(it) } // MUST use setText to use strings in editTexts
  }

  // Spinners Setup
  private fun setupSpinners() {
    val selectedPracticeType = arguments?.getString(CreateReportPracticeExtra) ?: ""
    EspressoIdlingResource.increment()
    employeeSpinner = viewBinding.employeeSpinner.apply { onItemSelectedListener = spinnerListener }
    locationSpinner = viewBinding.facilitySpinner.apply { onItemSelectedListener = spinnerListener }
    healthPracticeSpinner = viewBinding.healthPracticeSpinner.apply { onItemSelectedListener = spinnerListener }
    // Next starts loading spinner data, 1st w/ an emptyList then the full list from the server
    viewModel.adapterData.observe(viewLifecycleOwner) { (employeeList, healthPracticeList, locationList) ->
      // COULD keep refs to each arrayAdapter & mutate their lists via clear() then addAll(newList)
      // BUT making new adapters w/ each new list emitted and reSetting the spinners works just as well!
      ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, employeeList).also { employeeSpinner.adapter = it }
      ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, locationList).also { locationSpinner.adapter = it }
      ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, healthPracticeList).also {
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
        else -> Log.w("CreateReport ItemSelection", "Else statement ran for some reason!")
      }
    }
  }

  private inner class SubmitReportClickListener : View.OnClickListener {
    override fun onClick(clickedView: View) {
      if (viewModel.dateTimeString.value.isNullOrBlank()) { // Indicates date was likely not selected
        AlertDialog.Builder(requireContext()).run {
          setPositiveButton(R.string.alert_dialog_ok) { _, _ -> completeReportSubmission() }
          setNegativeButton(R.string.alert_dialog_cancel) { _, _ ->
            setFragmentResult(SnackbarDisplay,
              bundleOf(SnackbarBundleMessage to resources.getString(R.string.missing_date_hint)))
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
    setFragmentResult(CreateReportRequestKey, bundleOf())
    viewModel.submitReport()
    parentFragmentManager.popBackStack()
  }
}