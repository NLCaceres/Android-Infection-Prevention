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
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.Fade
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // BONUS: Fragments don't need to call requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS), they work by default!
    enterTransition = Fade(Fade.IN) // Like activity's window.enterTransition
    exitTransition = Fade(Fade.OUT) // AND window.exitTransition
    // Using androidx.transition.ChangeBounds for API 14 AndroidX improvements over old android.transition
    sharedElementEnterTransition = ChangeBounds()
    sharedElementReturnTransition = ChangeBounds() // Occurs on popback to slide it back in place in prev fragment
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentCreateReportBinding.inflate(layoutInflater)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    postponeEnterTransition()

    requireActivity().addMenuProvider(MenuProviderBase(findNavController()), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setupProgressIndicator()
    setupHeaderTextView()
    setupToast()
    setupDateEditText()
    setupSpinners()

    createReportButton = viewBinding.createReportButton.apply { setOnClickListener {
      if (viewModel.dateTimeString.value.isNullOrBlank()) { // Date likely was NOT selected, so alert user
        createAlertDialog().show(childFragmentManager, CreateReportAlertDialogTag)
      }
      else { completeReportSubmission() }
    }}
  }

  private fun setupProgressIndicator() {
    progressIndicator = viewBinding.progressIndicator.appProgressbar
    viewModel.loadingState.observe(viewLifecycleOwner) { loading ->
      progressIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
  }
  private fun setupHeaderTextView() {
    headerTV = viewBinding.headerTV // Should default to "New Observation" and will be changed when observer
    // Other sharedElement for following transition is HealthPracticeAdapter's precautionButtonTextView
    arguments?.getString(CreateReportPracticeExtra)?.let { selectedPractice ->
      ViewCompat.setTransitionName(headerTV, TransitionName(ReportTypeTextViewTransition, selectedPractice))
    } // Unclear if this transition runs if selectedPractice == "" (i.e. when arriving at this fragment via shortcut)

    viewModel.healthPracticeHeaderText.observe(viewLifecycleOwner) { headerTV.text = it } // emits a string from flow map func
  }
  private fun setupToast() {
    viewModel.toastMessage.observe(viewLifecycleOwner) { errMsg ->
      if (errMsg.isNotBlank()) {
        (activity as? ActivityMain)?.showSnackbar(errMsg)
        startPostponedEnterTransition()
      }
    }
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
      if (healthPracticeList.isNotEmpty()) { startPostponedEnterTransition() } // When data is ready, begin final transition
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

  private fun createAlertDialog(): DialogFragment {
    val title = resources.getString(R.string.date_alert_dialog_title)
    val message = resources.getString(R.string.date_alert_dialog_message)
    val missingDateHint = resources.getString(R.string.missing_date_hint)
    return AppFragmentAlertDialog.newInstance(title, message,
      okButtonListener = { completeReportSubmission() },
      cancelButtonListener = { (activity as? ActivityMain)?.showSnackbar(missingDateHint) }
    )
  }
  // Call this next fun either after user presses OK in alertDialog to use current time
  // OR after hitting submit because user correctly filled out all input
  private fun completeReportSubmission() {
    setFragmentResult(CreateReportRequestKey, bundleOf()) // Bundle required, so sending an empty one is fine
    viewModel.submitReport()
    findNavController().navigateUp() // NavComponent allows multi-step pop back if needed!
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _viewBinding = null
  }
}