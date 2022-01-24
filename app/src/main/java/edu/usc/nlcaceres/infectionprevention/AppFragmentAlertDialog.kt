package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.graphics.Point
import android.os.Build
import android.view.*
import edu.usc.nlcaceres.infectionprevention.databinding.CustomAlertDialogBinding
import android.view.WindowInsets

/* Custom DialogFragment, overrides onCreateView NOT onCreateDialog. Guide: https://bit.ly/3FUyvAy - Dialog with Custom Layout
Use: Display network errors - Shown by ActivityReportList & ActivityMain */
class AppFragmentAlertDialog : DialogFragment() {
  private lateinit var viewBinding : CustomAlertDialogBinding
  private lateinit var alertIcon : ImageView
  private lateinit var alertTitle : TextView
  private lateinit var alertMessage : TextView
  private lateinit var alertOKButton : Button
  private lateinit var alertCancelButton: Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    viewBinding = CustomAlertDialogBinding.inflate(inflater, container, false)
    return viewBinding.root // https://bit.ly/3mPAtLa - Android Custom Dialog info
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

//    alertIcon = view.findViewById<ImageView>(R.id.alertIcon).apply { setImageDrawable(ContextCompat.getDrawable(context, R.drawable.usc_shield_mono_gold)) }
    alertIcon = viewBinding.alertIcon.apply { setImageDrawable(ContextCompat.getDrawable(context, R.drawable.usc_shield_mono_gold)) }
    alertTitle = viewBinding.titleTextView.apply { text = arguments?.getString("Title") }
    alertMessage = viewBinding.alertMessage.apply { text = arguments?.getString("Message") }
    alertOKButton = viewBinding.alertOkButton.apply { setOnClickListener { dismiss() } }
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
    val windowSize = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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
      // Why use getSize? Injects the size info into our size var! BUT
      Point().also { display.getSize(it) } // Deprecated since return val changes if not called in an Activity
    }

    val width = windowSize.x
    //val height = windowSize.y
    window.setLayout((width * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT) // 75% of screen width
    //window.setLayout((width * 0.85).toInt(), (height * 0.3).toInt()) // Similar to above but height modded too
    window.setGravity(Gravity.CENTER)
  }

  companion object {
    fun newInstance(title : String, message : String, needCancelButton : Boolean) : AppFragmentAlertDialog {
      val frag = AppFragmentAlertDialog()
      frag.arguments = Bundle().apply {
        putString("Title", title)
        putString("Message", message)
        putBoolean("NeedCancelButton", needCancelButton)
      }
      return frag
    }
  }

}