package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.databinding.ItemHealthPracticeBinding
import androidx.core.view.ViewCompat
import edu.usc.nlcaceres.infectionprevention.util.TransitionName
import edu.usc.nlcaceres.infectionprevention.util.ReportTypeTextViewTransition

/* RecyclerView Adapter to render each particular HealthPractice button to launch FragmentCreateReport from a horizontal RV */
// Great new updates: viewBinding + ListAdapter's built-in DiffUtil
class HealthPracticeAdapter(private val healthPracticeClickListener : HealthPracticeClickListener) :
  ListAdapter<HealthPractice, ComposeHealthPracticeViewHolder>(HealthPracticeDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ComposeHealthPracticeViewHolder(ComposeView(parent.context))
//  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PracticeViewHolder(
//    ItemHealthPracticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ComposeHealthPracticeViewHolder, position: Int) {
    holder.bind(getItem(position), healthPracticeClickListener)
  }

  class PracticeViewHolder(private val viewBinding : ItemHealthPracticeBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(healthPractice : HealthPractice, listener : HealthPracticeClickListener) {
      viewBinding.precautionButtonTV.text = healthPractice.name.also {
        ViewCompat.setTransitionName(viewBinding.precautionButtonTV, TransitionName(ReportTypeTextViewTransition, it))
      }
      viewBinding.precautionImageView.contentDescription = "Create New ${healthPractice.name} Report"
      // Following click listener will not work with buttons, instead imageView is used (buttons seem to consume clicks, but not running callback)
      viewBinding.practiceItemView.setOnClickListener { itemView -> listener.onHealthPracticeItemClick(itemView, healthPractice) }
    }
  }
}

fun interface HealthPracticeClickListener {
  fun onHealthPracticeItemClick(view: View, healthPractice: HealthPractice)
}

/* Old fashioned way using baseAdapter - pass list + any other dependencies - use list to bindView + bind item in ViewHolder class */
//class HealthPracticeAdapter(private val practicesList : List<HealthPractice>, private val healthPracticeClickListener : HealthPracticeClickListener) :
//  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//  /* Even older way overrides containerView param, passes it to RecyclerView.ViewHolder() + also subclasses LayoutContainer */
//  class PracticeViewHolder(private val viewBinding : ItemHealthPracticeBinding) : RecyclerView.ViewHolder(viewBinding.root) {
//    fun bind(healthPractice : HealthPractice, listener : HealthPracticeClickListener) { }
//  }
//  /* Used to pass the ViewHolder with LayoutInflater.inflate(layoutResource, parent, false) */
//  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder { }
//  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) { }
//  override fun getItemCount(): Int { }
//  interface HealthPracticeClickListener { fun onHealthPracticeItemClick(view: View, healthPractice: HealthPractice) }
//}

private class HealthPracticeDiffCallback : DiffUtil.ItemCallback<HealthPractice>() {
  override fun areItemsTheSame(oldPractice: HealthPractice, newPractice: HealthPractice): Boolean =
    oldPractice.name == newPractice.name

  override fun areContentsTheSame(oldPractice: HealthPractice, newPractice: HealthPractice): Boolean =
    oldPractice.id == newPractice.id
}