package edu.usc.nlcaceres.infectionprevention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.helpers.Precaution
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_precautions.*

class AdapterPrecautionsRV(private val precautionsList : List<Precaution>, private val healthPracticeClickListener : AdapterPracticesRV.HealthPracticeClickListener) :
    RecyclerView.Adapter<AdapterPrecautionsRV.PrecautionViewHolder>() {

  private val viewPool = RecyclerView.RecycledViewPool()

  class PrecautionViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(precaution : Precaution, healthPracticeClickListener: AdapterPracticesRV.HealthPracticeClickListener) {
      precautionTypeTView.text = precaution.name
      val rvLayoutManager = LinearLayoutManager(containerView.context, LinearLayoutManager.HORIZONTAL, false)
      horizontalRecycleView.apply {
        layoutManager = rvLayoutManager
        precaution.practices?.let {
          adapter = AdapterPracticesRV(it, healthPracticeClickListener)
        }
      }
      precaution.practices?.let { if (it.size > 2) {
        backwardIndicatorArrow.visibility = View.VISIBLE; forwardIndicatorArrow.visibility = View.VISIBLE
        val alphaAnim = AlphaAnimation(0.0f, 1.0f).apply {
          duration = 1000; startOffset = 50; repeatMode = Animation.REVERSE; repeatCount = 5
          setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) { backwardIndicatorArrow.alpha = 0.0f; backwardIndicatorArrow.visibility = View.GONE
              forwardIndicatorArrow.alpha = 0.0f; forwardIndicatorArrow.visibility = View.GONE } // End on invisible
            override fun onAnimationRepeat(p0: Animation?) {}; override fun onAnimationStart(p0: Animation?) {}
          })
        }
        backwardIndicatorArrow.startAnimation(alphaAnim)
        forwardIndicatorArrow.startAnimation(alphaAnim)
      }}
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrecautionViewHolder = PrecautionViewHolder(LayoutInflater.
      from(parent.context).inflate(R.layout.item_precautions, parent, false))

  override fun onBindViewHolder(holder: PrecautionViewHolder, position: Int) {
    holder.bind(precautionsList[position], healthPracticeClickListener)
    holder.horizontalRecycleView.setRecycledViewPool(viewPool) // Saves some memory by making all parent/child use the same pool of items
  }

  override fun getItemCount(): Int = precautionsList.size
}