package edu.usc.nlcaceres.infectionprevention.util

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Resources.getSystem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

/* Dedicate reusable functions to use across Activities/Fragments */

// Returns true if Android Dark Theme is on (Available Android 10 / API 29+)
fun IsDarkMode(context: Context) = (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

fun SnakecaseString(string: String) = string.lowercase().replace(" ", "_")
fun TransitionName(prefix: String, id: String) = "$prefix.${SnakecaseString(id)}"

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

// Resources are actually ints so that's why upIndicator is an Int rather than a Drawable
fun ActionBar.setUpIndicator(upIndicator: Int) {
  setHomeAsUpIndicator(upIndicator)
  setDisplayHomeAsUpEnabled(true)
}

// If a fun takes pixels but would like to use densityIndependentPixels (dp), then easy conversion helpers!
fun dpUnits(desiredVal: Int) = (desiredVal * getSystem().displayMetrics.density).toInt()
fun pixels(dpUnits: Int) = (dpUnits / getSystem().displayMetrics.density).toInt()

// EditText Flow Helper
fun EditText.textUpdates(): Flow<CharSequence?> = callbackFlow { // Runs by default on scope and context of consumer
    // onTextChange params: text: CharSequence, start: 1st index of text (0 usually), before: previous count, count: size
    val textChangedListener = doOnTextChanged { text, _, _, _ -> trySend(text) }
    // awaitClose required! To remove listener later BUT ALSO ensure the flow lives as long as needed
    // Calling it means only the consumer OR a callback API that sends a SendChannel.close signal can finish the flow
    awaitClose { removeTextChangedListener(textChangedListener) }
}.onStart { emit(text) }.flowOn(Dispatchers.Main) // All values from trySend, get emitted onStart
// Can simply attach to a lifecycle via flowWithLifecycle AND launchIn
// OR do something a bit more complicated like below:
// editText.textChanges().filterNot { it.isNullOrBlank() }.debounce(300).distinctUntilChanged()
//    .flatMapLatest { executeSearch(it) }.onEach { updateUI(it) }.launchIn(lifecycleScope)
