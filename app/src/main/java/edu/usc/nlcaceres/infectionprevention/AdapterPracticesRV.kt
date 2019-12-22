package edu.usc.nlcaceres.infectionprevention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.usc.nlcaceres.infectionprevention.helpers.HealthPractice
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_health_practice.*

class AdapterPracticesRV(private val practicesList : List<HealthPractice>, private val healthPracticeClickListener : HealthPracticeClickListener) :
    RecyclerView.Adapter<AdapterPracticesRV.PracticeViewHolder>() {
  class PracticeViewHolder(override val containerView : View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(healthPractice: HealthPractice, listener : HealthPracticeClickListener) {
      precautionButtonTV.text = healthPractice.name
      practiceItemView.setOnClickListener { listener.onHealthPracticeItemClick(it, healthPractice) }
      // The above click listener will not work with buttons, instead imageView is used (buttons seem to consume clicks, not running above click)
    }
  }

  override fun getItemCount(): Int = practicesList.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeViewHolder = PracticeViewHolder(LayoutInflater.
      from(parent.context).inflate(R.layout.item_health_practice, parent, false))

  override fun onBindViewHolder(holder: PracticeViewHolder, position: Int) {
    holder.bind(practicesList[position], healthPracticeClickListener)
  }

  interface HealthPracticeClickListener {
    fun onHealthPracticeItemClick(view: View, healthPractice: HealthPractice)
  }
}