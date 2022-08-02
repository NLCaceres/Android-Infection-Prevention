package edu.usc.nlcaceres.infectionprevention.util

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import edu.usc.nlcaceres.infectionprevention.R

/* Dedicate reusable functions to use across Activities/Fragments */

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