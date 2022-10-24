package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.os.bundleOf
import com.google.android.material.navigation.NavigationView
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.util.preSelectedFilterExtra

class NavDrawerItemSelectedListener(private val finalizeBundle: (Bundle) -> Unit)
  : NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
      val reportListBundle = bundleOf()

      when (item.itemId) { // Add filter for ReportList View
        R.id.nav_reports -> {
          Log.d("Nav Item Selected", "Clicked General Report Item")
        }
        R.id.nav_standard_precautions -> {
          reportListBundle.putParcelable(preSelectedFilterExtra, FilterItem("Standard", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Standard Precautions Report Item")
        }
        R.id.nav_isolation_precautions -> {
          reportListBundle.putParcelable(preSelectedFilterExtra, FilterItem("Isolation", true, "Precaution Type"))
          Log.d("Nav Item Selected", "Clicked Isolation Precautions Report Item")
        }
        else -> return false
      }

      finalizeBundle(reportListBundle)
      return true
    }
}

