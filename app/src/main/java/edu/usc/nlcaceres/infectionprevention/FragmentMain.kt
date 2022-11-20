package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.doOnPreDraw
import androidx.transition.Slide
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentMainBinding
import edu.usc.nlcaceres.infectionprevention.util.*
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelMain

@AndroidEntryPoint
class FragmentMain: Fragment(R.layout.fragment_main) {
  private val viewModel: ViewModelMain by activityViewModels()
  // Why have a nullable viewBinding? Because fragments can outlive their views!
  private var _viewBinding: FragmentMainBinding? = null
  private val viewBinding get() = _viewBinding!! // So only access from this prop and we can guarantee null safety!
  private lateinit var progIndicator : ProgressBar
  private lateinit var sorryMsgTextView : TextView
  private lateinit var precautionRecyclerView : RecyclerView
  private lateinit var precautionAdapter : PrecautionAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    exitTransition = Slide(Gravity.LEFT)

    // Following is similar idea to a trampoline activity for shortcuts: Check intent & replace fragment
    if (requireActivity().intent.action == ShortcutIntentAction) { // If launched from shortcut
      parentFragmentManager.commit { // Begin transaction to FragmentCreateReport
        setReorderingAllowed(true)
        addToBackStack(null)
        replace<FragmentCreateReport>(R.id.fragment_main_container)
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentMainBinding.inflate(inflater, container, false)
    return viewBinding.root
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // MUST delay transition until recyclerview loads & renders all its data so its itemViews can animate properly
    postponeEnterTransition() // Call start in precautionStateObserver below

    (activity as AppCompatActivity).supportActionBar?.setUpIndicator(R.drawable.ic_menu)
    requireActivity().addMenuProvider(FragmentMainMenu(), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setupStateViews()
    setupPrecautionRV()
  }

  private inner class FragmentMainMenu: MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      menuInflater.inflate(R.menu.action_buttons, menu)
    }
    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
      android.R.id.home -> { setFragmentResult(NavDrawerManager, bundleOf(NavDrawerBundleOpener to true)); true }
      R.id.action_settings -> {
        parentFragmentManager.commit {
          setReorderingAllowed(true)
          addToBackStack(null)
          replace<FragmentSettings>(R.id.fragment_main_container)
        }
        true
      }
      else -> false
    }
  }

  private fun setupStateViews() {
    progIndicator = viewBinding.progressIndicatorLayout.appProgressbar
    viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
      progIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    sorryMsgTextView = viewBinding.sorryTextView
    viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
      // This will ONLY ever receive a value if the precautionState liveData fails!
      // SO NO POINT observing the message from precautionState, it couldn't ever receive it due to the flow crashing
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        with(sorryMsgTextView) {
          visibility = if (viewModel.precautionListEmpty()) View.VISIBLE else View.INVISIBLE
          text = message
        }
        setFragmentResult(SnackbarDisplay, bundleOf(SnackbarBundleMessage to message))
      }
    }
  }

  private fun setupPrecautionRV() {
    precautionRecyclerView = viewBinding.precautionRV.apply {
      setHasFixedSize(true)
      precautionAdapter = PrecautionAdapter { itemView, healthPractice ->
        // Set the transitionName to ID the view in BOTH HealthPracticeAdapter & FragmentCreateReport
        val reportTypeTV = itemView.findViewById<View>(R.id.precautionButtonTV)
        val createReportBundle = bundleOf(CreateReportPracticeExtra to healthPractice.name)
        parentFragmentManager.commit { // AND perform replacement w/ setReordering & addSharedElement
          setReorderingAllowed(true) // Ensure animation occurs w/ sharedElement for this transaction
          addSharedElement(reportTypeTV, TransitionName(ReportTypeTextViewTransition, healthPractice.name))
          addToBackStack(null) // i.e. Use this transition on this createReport replace
          replace<FragmentCreateReport>(R.id.fragment_main_container, args = createReportBundle)
        }
      }
      adapter = precautionAdapter
    }
    // Set listener for FragmentCreateReport so we can react to submissions and go to FragmentReportList
    parentFragmentManager.setFragmentResultListener(CreateReportRequestKey, viewLifecycleOwner) { requestKey, _ ->
      if (requestKey != CreateReportRequestKey) { return@setFragmentResultListener } // SHOULD ALWAYS receive this key
      val (precautionNames, healthPracticeNames) = viewModel.getNamesLists()
      val reportListBundle = bundleOf(PrecautionListExtra to precautionNames, HealthPracticeListExtra to healthPracticeNames)
      parentFragmentManager.commit {
        setReorderingAllowed(true)
        addToBackStack(null)
        replace<FragmentReportList>(R.id.fragment_main_container, args = reportListBundle) // Add() just stacks the view on top
      }
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
      if (newList.isNotEmpty()) { // Only start transition if fragment's parentView has drawn/laid out all children
        (view?.parent as? ViewGroup)?.doOnPreDraw { startPostponedEnterTransition() }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _viewBinding = null
  }
}