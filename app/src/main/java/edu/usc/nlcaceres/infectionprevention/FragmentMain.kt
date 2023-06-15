package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentMainBinding
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

/* Fragment listing Buttons grouped by Precaution type, each Button corresponds to possible HealthPractice violations
* Each Button leads to FragmentCreateReport, populating the new Report with the selected HealthPractice violation
* Launches from: App Icon. Also, briefly, as a trampoline for FragmentCreateReport Shortcut */
@AndroidEntryPoint
class FragmentMain: Fragment(R.layout.fragment_main) {
  private val viewModel: ViewModelMain by activityViewModels()
  // Why have a nullable viewBinding? Because fragments can outlive their views!
  private var _viewBinding: FragmentMainBinding? = null
  private val viewBinding get() = _viewBinding!! // So only access from this prop, and we can guarantee null safety!
  private lateinit var progIndicator : ProgressBar
  private lateinit var sorryMsgTextView : TextView
  private lateinit var precautionRecyclerView : RecyclerView
  private lateinit var precautionAdapter : PrecautionAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    exitTransition = Slide(Gravity.LEFT)

    // Following is similar idea to a trampoline activity for shortcuts: Check intent & replace fragment
    if (requireActivity().intent.action == ShortcutIntentAction) { // If launched from shortcut
      // NavComponent makes fragment transactions very easy especially w/ Safe-Args adding Directions to use to navigate
      findNavController().navigate(FragmentMainDirections.actionToCreateReportFragment())
    } // No fragmentManager.commit{} needed, nor a replace<FragmentType>(fragmentContainer, args) call +
    // Auto-adds to back stack & no setReorderingAllowed(true) for optimization, particularly w/ animations/transitions
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentMainBinding.inflate(inflater, container, false)
    return viewBinding.root
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // MUST delay transition until recyclerview loads & renders all its data so its itemViews can animate properly
    postponeEnterTransition() // Call start in precautionStateObserver below

    requireActivity().addMenuProvider(MenuProviderBase(findNavController()), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setupStateViews()
    setupPrecautionRV()
  }

  private fun setupStateViews() {
    progIndicator = viewBinding.progressIndicatorLayout.appProgressbar
    viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
      progIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    sorryMsgTextView = viewBinding.sorryTextView
    viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
      // This will ONLY ever receive a value if the precautionState liveData fails!
      // SO NO POINT observing the message from precautionState, it couldn't ever receive it due to the flow crashing
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        with(sorryMsgTextView) {
          visibility = if (viewModel.precautionListEmpty()) View.VISIBLE else View.INVISIBLE
          text = message
        }
        (activity as? ActivityMain)?.showSnackbar(message)
        startPostponedEnterTransition() // Since precautionStateObserver won't call it, MUST be called here!
      }
    }
  }

  private fun setupPrecautionRV() {
    precautionRecyclerView = viewBinding.precautionRV.apply {
      setHasFixedSize(true)
      precautionAdapter = PrecautionAdapter { itemView, healthPractice ->
        // Set the transitionName to ID the view in BOTH HealthPracticeAdapter & FragmentCreateReport
        val reportTypeTV = itemView.findViewById<View>(R.id.precautionButtonTV)
        val navExtras = FragmentNavigatorExtras(reportTypeTV to TransitionName(ReportTypeTextViewTransition, healthPractice.name))
        // Instead of fragmentManager.commit w/ setReorderingAllowed(true), addSharedElement(view, transitionName),
        // addToBackStack(null) + replace(fragmentContainer, args), Just let the NavComponent handle most of it!
        val actionDirections = FragmentMainDirections.actionToCreateReportFragment(healthPractice.name)
        findNavController().navigate(actionDirections, navExtras)
      }
      adapter = precautionAdapter
    }
    // Set listener for FragmentCreateReport, so we can react to submissions and go to FragmentReportList
    parentFragmentManager.setFragmentResultListener(CreateReportRequestKey, viewLifecycleOwner) { requestKey, _ ->
      if (requestKey != CreateReportRequestKey) { return@setFragmentResultListener } // SHOULD ALWAYS receive this key
      // NavComponent makes transitions even simpler (as long as it doesn't need Navigator Extras like for Transitions)
      findNavController().navigate(R.id.reportListFragment) // One simple line rather than 5 w/ fragmentManager.commit {}
    }

    viewModel.precautionState.observe(viewLifecycleOwner) { (loading, newList) ->
      precautionAdapter.submitList(newList)
      with(sorryMsgTextView) {
        visibility = if (newList.isEmpty()) View.VISIBLE else View.INVISIBLE
        text = when {
          loading -> "Looking up precautions"
          newList.isEmpty() -> "Weird! Seems we don't have any available precautions to choose from!"
          else -> "Please try again later!"
        }
      }
      if (!loading) { // Only start transition if fragment's parentView has drawn/laid out all children
        (view?.parent as? ViewGroup)?.doOnPreDraw { startPostponedEnterTransition() }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _viewBinding = null // Prevent access to view if Fragment paused but view destroyed
  }
}