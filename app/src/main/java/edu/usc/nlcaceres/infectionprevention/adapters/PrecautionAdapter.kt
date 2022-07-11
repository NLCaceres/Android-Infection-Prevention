package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.databinding.ItemPrecautionBinding

/* Adapter to render sets of buttons to launch CreateReportActivity based on precaution type */
class PrecautionAdapter(private val healthPracticeClickListener : HealthPracticeClickListener) :
  ListAdapter<Precaution, PrecautionAdapter.PrecautionViewHolder>(PrecautionDiffCallback()) {

  private val viewPool = RecyclerView.RecycledViewPool()

  class PrecautionViewHolder(val viewBinding: ItemPrecautionBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(precaution : Precaution, healthPracticeClickListener : HealthPracticeClickListener) {
      viewBinding.run {
        precautionTypeTView.text = itemView.context.resources.getString(R.string.create_report_header, precaution.name)
        val rvLayoutManager = LinearLayoutManager(null, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecycleView.apply {
          layoutManager = rvLayoutManager
          adapter = HealthPracticeAdapter(healthPracticeClickListener)
          (adapter as HealthPracticeAdapter).submitList(precaution.practices)
        }
        /* Since the healthPracticeRV scrolls horizontally, more than 2 items means the 3rd+ item will be off screen
        * so following animation intends to let user know they can scroll, displaying arrows then slowly hiding them */
        precaution.practices?.let { if (it.size > 2) {
          backwardIndicatorArrow.visibility = View.VISIBLE; forwardIndicatorArrow.visibility = View.VISIBLE
          val alphaAnim = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000; startOffset = 50; repeatMode = Animation.REVERSE; repeatCount = 5
            setAnimationListener(object : Animation.AnimationListener {
              override fun onAnimationEnd(p0: Animation?) {
                backwardIndicatorArrow.alpha = 0.0f; backwardIndicatorArrow.visibility = View.GONE
                forwardIndicatorArrow.alpha = 0.0f; forwardIndicatorArrow.visibility = View.GONE
              } // End on invisible
              override fun onAnimationRepeat(p0: Animation?) {}; override fun onAnimationStart(p0: Animation?) {}
            })
          }
          backwardIndicatorArrow.startAnimation(alphaAnim)
          forwardIndicatorArrow.startAnimation(alphaAnim)
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
    oldPrecaution.practices == newPrecaution.practices // Should just work thanks to kotlin data classes (which override equals)
}
