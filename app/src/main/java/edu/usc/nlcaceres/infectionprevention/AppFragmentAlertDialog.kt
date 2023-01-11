package edu.usc.nlcaceres.infectionprevention

import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.WindowInsets
import android.view.Gravity
import java.io.Serializable
import edu.usc.nlcaceres.infectionprevention.databinding.CustomAlertDialogBinding
import edu.usc.nlcaceres.infectionprevention.util.AlertDialogBundleTitleKey
import edu.usc.nlcaceres.infectionprevention.util.AlertDialogBundleMessageKey
import edu.usc.nlcaceres.infectionprevention.util.AlertDialogBundleOkButtonListenerKey
import edu.usc.nlcaceres.infectionprevention.util.AlertDialogBundleNeedBasicCancelButtonKey
import edu.usc.nlcaceres.infectionprevention.util.AlertDialogBundleCancelButtonListenerKey
import edu.usc.nlcaceres.infectionprevention.util.fetchSerializable

/* Custom DialogFragment, overrides onCreateView NOT onCreateDialog.
Guide: https://developer.android.com/guide/topics/ui/dialogs - Dialog with Custom Layout
Use: Display network errors - Shown by FragmentReportList & ActivityMain */
class AppFragmentAlertDialog : DialogFragment() {
  private lateinit var viewBinding : CustomAlertDialogBinding
  private lateinit var alertIcon : ImageView
  private lateinit var alertTitle : TextView
  private lateinit var alertMessage : TextView
  private lateinit var alertOKButton : Button
  private lateinit var alertCancelButton: Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    viewBinding = CustomAlertDialogBinding.inflate(inflater, container, false)
    return viewBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    alertIcon = viewBinding.alertIcon.apply { setImageDrawable(ContextCompat.getDrawable(context, R.drawable.usc_shield_mono_gold)) }
    alertTitle = viewBinding.alertTitle.apply { text = arguments?.getString(AlertDialogBundleTitleKey) }
    alertMessage = viewBinding.alertMessage.apply { text = arguments?.getString(AlertDialogBundleMessageKey) }
    alertOKButton = viewBinding.alertOkButton.apply {
      setOnClickListener {
        (arguments?.fetchSerializable<ButtonListener>(AlertDialogBundleOkButtonListenerKey))?.onClick()
        dismiss()
      }
    }
    alertCancelButton = viewBinding.alertCancelButton.apply {
      val cancelButtonListener = arguments?.fetchSerializable<ButtonListener>(AlertDialogBundleCancelButtonListenerKey)
      val needCancelButton = (arguments?.getBoolean(AlertDialogBundleNeedBasicCancelButtonKey) ?: false)
      visibility = if (needCancelButton) View.VISIBLE else View.INVISIBLE
      setOnClickListener { cancelButtonListener?.onClick(); dismiss() } // Run custom cancel block then dismiss dialog
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

    val width = windowSize.x * 0.9 // Width will actually be slightly under 90% of screen width (due to insets)
    window.setLayout(width.toInt(), WindowManager.LayoutParams.WRAP_CONTENT) // Height will wrap content
    window.setGravity(Gravity.CENTER)
  }

  // Following used to make button callbacks that thanks to Serializable can be passed into bundle
  fun interface ButtonListener: Serializable {
    fun onClick() // Could provide a param ref to THIS dialog instance like DialogInterface does but currently not needed
  }

  companion object {
    // Why newInstance rather than constructor? Bundle survives configuration changes + recreation
    // ViewModel survives config changes BUT not recreation. During recreation the empty default constructor is called
    fun newInstance(title: String, message: String, okButtonListener: ButtonListener? = null,
                    needCancelButton: Boolean? = null, cancelButtonListener: ButtonListener? = null): AppFragmentAlertDialog {
      val frag = AppFragmentAlertDialog()
      frag.arguments = Bundle().apply {
        putString(AlertDialogBundleTitleKey, title)
        putString(AlertDialogBundleMessageKey, message)
        putSerializable(AlertDialogBundleOkButtonListenerKey, okButtonListener)
        // Why needCancelButton flag AND a cancelButtonListener?
        // If just using the flag, then cancelButton is visible & fires a simple dismiss w/out any bonus functionality
        // If just using the cancelButtonListener, then flag becomes true anyway, making the button visible BUT w/ bonus functionality
        putBoolean(AlertDialogBundleNeedBasicCancelButtonKey, needCancelButton ?: false || cancelButtonListener != null)
        putSerializable(AlertDialogBundleCancelButtonListenerKey, cancelButtonListener)
      }
      return frag
    }
  }

}