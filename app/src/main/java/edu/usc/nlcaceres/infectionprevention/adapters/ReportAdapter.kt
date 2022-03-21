package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

/* Adapter for ReportsListActivity RecyclerView */
// If not specifying specific ViewHolder class, then some casting needed. See ln 37.
class ReportAdapter : ListAdapter<Report, RecyclerView.ViewHolder>(ReportDiffCallback()) {

  /* View contained in each item of the RecyclerView */
  class ReportViewHolder(private val viewBinding : ItemReportBinding) : RecyclerView.ViewHolder(viewBinding.root) /*, LayoutContainer */ {
    // init { } // Init blocks in kotlin run right after the constructor finishes (so useful for clickListener setup!)
    fun bind(report : Report) {
      //TODO: Add reportTypeImageView?
      val reportTypeText = "${report.healthPractice?.name} Violation"
      viewBinding.reportTypeTitleTV.text = reportTypeText
      viewBinding.reportDateTV.text = report.date?.let { SimpleDateFormat("MMM dd, yyyy h:mma", Locale.getDefault()).format(it) }
      val reportEmployeeText = "Committed by ${report.employee?.fullName}"
      viewBinding.reportEmployeeNameTV.text = reportEmployeeText
      val locationText = "Location: ${report.location?.facilityName} Unit: ${report.location?.unitNum} Room: ${report.location?.roomNum}"
      viewBinding.reportLocationTV.text = locationText
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder = ReportViewHolder(
    ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))
// ReportViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)) // Original return val

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as ReportViewHolder).bind(getItem(position)) // Used to grab report from the dataset (reportsList) before!
  }

//  override fun getItemCount(): Int = reportsList.size

  /* Don't need our own updateData func anymore! ListAdapter now has submitList() that runs DiffUtil.calculateDiff() followed by
  * diffResult.dispatchUpdatesTo(this) w/ our ReportDiffCallback class, no explicit list clearing or data manipulation needed here  */
}

private class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
  /* Used to have a getOldListSize + getNewListSize funs BUT no longer needed!
  * Moreover, areItems + areContentsTheSame signature has changed to dataClass instead of position ints */
  override fun areItemsTheSame(oldReport: Report, newReport: Report): Boolean = oldReport.id == newReport.id

  override fun areContentsTheSame(oldReport: Report, newReport: Report): Boolean = oldReport == newReport
  // Called only if areItemsTheSame() returns TRUE // Alternatively destructure like below and compare as needed
  // val (_, oldEmployee, oldPractice, oldLocation, oldDate) = oldList[oldItemPosition]
  // val (_, newEmployee, newPractice, newLocation, newDate) = newList[oldItemPosition]
}
