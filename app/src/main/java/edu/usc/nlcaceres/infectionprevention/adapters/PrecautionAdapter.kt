package edu.usc.nlcaceres.infectionprevention.adapters

import android.animation.AnimatorSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DiffUtil
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.databinding.ItemPrecautionBinding
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import edu.usc.nlcaceres.infectionprevention.util.MarginsItemDecoration
import edu.usc.nlcaceres.infectionprevention.util.createFlashingAnimation
import edu.usc.nlcaceres.infectionprevention.util.dpUnits

/* Adapter to render sets of buttons to launch CreateReportActivity based on precaution type */
class PrecautionAdapter(private val healthPracticeClickListener : HealthPracticeClickListener) :
  ListAdapter<Precaution, PrecautionAdapter.PrecautionViewHolder>(PrecautionDiffCallback()) {

  private val viewPool = RecyclerView.RecycledViewPool()

  class PrecautionViewHolder(val viewBinding: ItemPrecautionBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(precaution : Precaution, healthPracticeClickListener : HealthPracticeClickListener) {
      viewBinding.run {
        precautionTypeTView.text = itemView.context.resources.getString(R.string.create_report_header, precaution.name)
        horizontalRecycleView.apply { // Horizontal scroll thanks to above layoutManager
          layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
          addItemDecoration(MarginsItemDecoration(dpUnits(30), orientation = DividerItemDecoration.VERTICAL))
          adapter = HealthPracticeAdapter(healthPracticeClickListener)
          EspressoIdlingResource.increment()
          (adapter as HealthPracticeAdapter).submitList(precaution.healthPractices)
          EspressoIdlingResource.decrement()
        }
        // > 2 items means the rest may be off screen so animating the arrows SHOULD let users know they can scroll
        precaution.healthPractices.let { if (it.size > 2) { // Can use imageView.context w/ imageView.drawable.setTint to change arrows' color
          backwardIndicatorArrow.visibility = View.VISIBLE; forwardIndicatorArrow.visibility = View.VISIBLE
          // Could call animate() on the indicators as a quicker way to animate the alpha property BUT
          // animate() is better for animating multiple properties on 1 view, not coordinating two views w/ a single prop animation
          val backArrowAnim = createFlashingAnimation(backwardIndicatorArrow)
          val forwardArrowAnim = createFlashingAnimation(forwardIndicatorArrow)
          // Arrows start visible and end invisible. Below fires off the animations at the same time
          AnimatorSet().apply { playTogether(backArrowAnim, forwardArrowAnim); start() }
        }}
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrecautionViewHolder = PrecautionViewHolder(
    ItemPrecautionBinding.inflate(LayoutInflater.from(parent.context), parent,false))

  override fun onBindViewHolder(holder: PrecautionViewHolder, position: Int) {
    holder.bind(getItem(position), healthPracticeClickListener)
    holder.viewBinding.horizontalRecycleView.setRecycledViewPool(viewPool) // Save some memory by making parent+child to use the same pool of items
  }
}

private class PrecautionDiffCallback : DiffUtil.ItemCallback<Precaution>() {
  override fun areItemsTheSame(oldPrecaution: Precaution, newPrecaution: Precaution): Boolean =
    oldPrecaution.name == newPrecaution.name

  override fun areContentsTheSame(oldPrecaution: Precaution, newPrecaution: Precaution): Boolean =
    oldPrecaution.healthPractices == newPrecaution.healthPractices // Should just work thanks to kotlin data classes (which override equals)
}
