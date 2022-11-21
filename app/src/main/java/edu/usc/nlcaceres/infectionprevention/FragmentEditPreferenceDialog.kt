package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.Window
import android.view.WindowManager
import android.view.Gravity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.preference.PreferenceManager
import edu.usc.nlcaceres.infectionprevention.databinding.PreferencesEdittextDialogBinding
import edu.usc.nlcaceres.infectionprevention.util.EditTextDialogRequestKey
import edu.usc.nlcaceres.infectionprevention.util.EditTextDialogPreferenceKey
import edu.usc.nlcaceres.infectionprevention.util.EditTextDialogPreferenceValue

class FragmentEditPreferenceDialog: DialogFragment() {
  private lateinit var viewBinding : PreferencesEdittextDialogBinding
  private lateinit var alertIcon : ImageView
  private lateinit var alertTitle : TextView
  private lateinit var alertEditText: EditText
  private lateinit var alertOKButton : Button
  private lateinit var alertCancelButton: Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    viewBinding = PreferencesEdittextDialogBinding.inflate(inflater, container, false)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
    val key = arguments?.getString("Key")
    val currentVal = sharedPrefs.getString(key, "") // Returns currentVal or our default, empty string, ""

    alertIcon = viewBinding.alertIcon.apply { setImageDrawable(ContextCompat.getDrawable(context, R.drawable.usc_shield_mono_gold)) }
    alertTitle = viewBinding.title.apply { text = arguments?.getString("Title") }
    alertEditText = viewBinding.edit.apply {
      hint = arguments?.getString("Hint")
      setText(currentVal)
    }
    alertOKButton = viewBinding.alertOkButton.apply { setOnClickListener {
      val newVal = alertEditText.text.toString()
      sharedPrefs.edit { // Will run apply() to update immediately to sharedPrefs and async-ly to the disk
        putString(key, newVal) // Grab text from editText to set new val
      } // Could add commit = true param BUT commit() could freeze since it sync-ly saves to disk
      setFragmentResult(EditTextDialogRequestKey,
        bundleOf(EditTextDialogPreferenceKey to key, EditTextDialogPreferenceValue to newVal))
      dismiss()
    }}
    alertCancelButton = viewBinding.alertCancelButton.apply {
      val needCancelButton = arguments?.getBoolean("NeedCancelButton") ?: false
      visibility = if (needCancelButton) View.VISIBLE else View.INVISIBLE
      setOnClickListener { dismiss() }
    }
  }

  override fun onResume() {
    super.onResume()
    dialog?.run { window?.let { setUpDimensions(it) } }
  }

  private fun setUpDimensions(window: Window) {
    val windowSize = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) { // Q == SDK 29 Android 10
      val windowMetrics = window.windowManager.currentWindowMetrics
      // Gets all insets (system bars, nav bars, display cutouts)
      val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
        WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
      val insetsWidth: Int = insets.right + insets.left
      val insetsHeight: Int = insets.top + insets.bottom

      // Subtract away insets so we get ONLY app window size (return Point() rather than Size())
      Point(windowMetrics.bounds.width() - insetsWidth, windowMetrics.bounds.height() - insetsHeight)
      // The last expression in a Kotlin if/else blocks get treated like return vals
    }
    else { // How display.getSize used to calculate (rather than use currentWindowMetrics.bounds)
      val display = window.windowManager.defaultDisplay
      // Why use getSize? Injects the dimensions into our windowSize var! BUT
      Point().also { display.getSize(it) } // Deprecated since return val changes if not called in an Activity
    }

    val width = windowSize.x * 0.9 // Slightly smaller than 90% of screen width
    window.setLayout(width.toInt(), WindowManager.LayoutParams.WRAP_CONTENT) // Height wraps content
    window.setGravity(Gravity.CENTER)
  }

  companion object {
    fun newInstance(key: String, title : String, hint : String, needCancelButton : Boolean)
      : FragmentEditPreferenceDialog {
        val frag = FragmentEditPreferenceDialog()
        frag.arguments = Bundle().apply {
          putString("Key", key) // Used to update text value
          putString("Title", title) // For textview
          putString("Hint", hint) // For editText hint
          putBoolean("NeedCancelButton", needCancelButton)
        }
        return frag
    }
  }
}