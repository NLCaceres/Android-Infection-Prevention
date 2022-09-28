package edu.usc.nlcaceres.infectionprevention.util

import android.content.res.Resources.getSystem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.usc.nlcaceres.infectionprevention.R

/* Dedicate reusable functions to use across Activities/Fragments */

// Quick reusable Snackbar display func AND handles Espresso, a common pain point with snackbars/toasts
fun ShowSnackbar(view: CoordinatorLayout, text: String, duration: Int) {
    Snackbar.make(view, text, duration).run {
      addCallback(object : Snackbar.Callback() {
        override fun onShown(sb: Snackbar?) { EspressoIdlingResource.increment() }
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) { EspressoIdlingResource.decrement() }
      })
      show()
    }
}

// May be useful for more than a progressBar but most common case
fun HideProgressIndicator(hidden : Boolean, progressBar : ProgressBar) {
    progressBar.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
}

/* Toolbar across activities all setup fairly similar so consolidate! */
// Resources are actually ints so that's why upIndicator is an Int rather than a Drawable
fun SetupToolbar(activity: AppCompatActivity, toolbar: Toolbar, upIndicator: Int = 0, title: String = ""): Toolbar {
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.apply {
        setHomeAsUpIndicator(upIndicator) // Override up button (so back button)
        setDisplayHomeAsUpEnabled(true)
        if (title != "") { // Explicitly passing " " (as opposed to default "") sets an empty title
            setTitle(title)
            toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.colorLight))
        }
    }
    return toolbar
}

// If a fun takes pixels but would like to use densityIndependentPixels (dp), then easy conversion helpers!
fun dpUnits(desiredVal: Int) = (desiredVal * getSystem().displayMetrics.density).toInt()
fun pixels(dpUnits: Int) = (dpUnits / getSystem().displayMetrics.density).toInt()
