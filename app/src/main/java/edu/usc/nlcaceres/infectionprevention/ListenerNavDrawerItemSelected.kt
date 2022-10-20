package edu.usc.nlcaceres.infectionprevention

import android.content.Intent
import android.util.Log
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.util.preSelectedFilterExtra

class NavDrawerItemSelectedListener(private val createIntent: () -> Intent, private val finalizeIntent: (Intent) -> Unit)
  : NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
      val reportListIntent = createIntent()

      when (item.itemId) { // Add filter for ReportListActivity
        R.id.nav_reports -> {
          Log.d("Nav Item Selected", "Clicked General Report Item")
        }
        R.id.nav_standard_precautions -> {
          reportListIntent.putExtra(preSelectedFilterExtra, FilterItem("Standard", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Standard Precautions Report Item")
        }
        R.id.nav_isolation_precautions -> {
          reportListIntent.putExtra(preSelectedFilterExtra, FilterItem("Isolation", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Isolation Precautions Report Item")
        }
        else -> return false
      }

      finalizeIntent(reportListIntent)
      return true
    }
}

