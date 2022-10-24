package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.usc.nlcaceres.infectionprevention.adapters.PrecautionAdapter
import edu.usc.nlcaceres.infectionprevention.databinding.FragmentMainBinding
import edu.usc.nlcaceres.infectionprevention.util.ShowSnackbar
import edu.usc.nlcaceres.infectionprevention.util.createReportPracticeExtra
import edu.usc.nlcaceres.infectionprevention.util.setUpIndicator
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

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _viewBinding = FragmentMainBinding.inflate(inflater, container, false)
    return viewBinding.root
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

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
      android.R.id.home -> { (activity as? ActivityMain)?.navDrawer?.openDrawer(GravityCompat.START); true }
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
    viewModel.isLoading.observe(viewLifecycleOwner) { loading -> progIndicator.visibility = if (loading) View.VISIBLE else View.INVISIBLE }

    sorryMsgTextView = viewBinding.sorryTextView
    viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
      // This will ONLY ever receive a value if the precautionState liveData fails!
      // SO NO POINT observing the message from precautionState, it couldn't ever receive it due to the flow crashing
      if (message.isNotBlank()) { // Can't be empty ("") or just whitespace ("   ")
        with(sorryMsgTextView) {
          visibility = if (viewModel.precautionListEmpty()) View.VISIBLE else View.INVISIBLE
          text = message
        }
        ShowSnackbar((activity as ActivityMain).coordinatorLayout, message, Snackbar.LENGTH_SHORT)
      }
    }
  }

  private val createReportActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val bundle = bundleOf()
      val precautions = viewModel.precautionState.value?.second ?: emptyList()
      (activity as ActivityMain).addPrecautionsAndHealthPractices(bundle, precautions)
      parentFragmentManager.commit {
        setReorderingAllowed(true)
        addToBackStack(null)
        replace<FragmentReportList>(R.id.fragment_main_container, args = bundle) // Add() just stacks the view on top
      }
    }
  }
  private fun setupPrecautionRV() {
    precautionRecyclerView = viewBinding.precautionRV.apply {
      setHasFixedSize(true)
      precautionAdapter = PrecautionAdapter { itemView, healthPractice ->
        val reportTypeTV = itemView.findViewById<View>(R.id.precautionButtonTV)
        // Click Listener that creates an intent and launches the CreateReport Activity
        Intent(context, ActivityCreateReport::class.java).apply {
          putExtra(createReportPracticeExtra, healthPractice.name)
        }.also {
          createReportActivityLauncher.launch(it,
            ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), reportTypeTV, "reportType"))
        }
      }
      adapter = precautionAdapter
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
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _viewBinding = null
  }
}