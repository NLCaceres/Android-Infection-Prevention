package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.databinding.ItemHealthPracticeBinding

/* Adapter to render each particular health practice button in a horizontal RV to launch CreateReportActivity */
// Great new updates: viewBinding + built in DiffUtil via ListAdapter!
class HealthPracticeAdapter(private val healthPracticeClickListener : HealthPracticeClickListener) :
  ListAdapter<HealthPractice, HealthPracticeAdapter.PracticeViewHolder>(HealthPracticeDiffCallback()) {

  class PracticeViewHolder(private val viewBinding : ItemHealthPracticeBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(healthPractice : HealthPractice, listener : HealthPracticeClickListener) {
      viewBinding.precautionButtonTV.text = healthPractice.name
      viewBinding.precautionImageView.contentDescription = "Create New ${healthPractice.name} button"
      // Following click listener will not work with buttons, instead imageView is used (buttons seem to consume clicks, but not running callback)
      viewBinding.practiceItemView.setOnClickListener { itemView -> listener.onHealthPracticeItemClick(itemView, healthPractice) }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeViewHolder = PracticeViewHolder(
    ItemHealthPracticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: PracticeViewHolder, position: Int) {
    holder.bind(getItem(position), healthPracticeClickListener)
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