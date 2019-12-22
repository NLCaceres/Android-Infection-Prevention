package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.view.Gravity
import android.view.WindowManager
import android.graphics.Point



class AppFragmentAlertDialog : DialogFragment() {
  private lateinit var alertIcon : ImageView
  private lateinit var alertTitle : TextView
  private lateinit var alertMessage : TextView
  private lateinit var alertOKButton : Button
  private lateinit var alertCancelButton: Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.custom_alert_dialog, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val args = arguments

    alertIcon = view.findViewById<ImageView>(R.id.alertIcon).apply { setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.usc_shield_mono_gold)) }
    alertTitle = view.findViewById<TextView>(R.id.titleTextView).apply { text = args?.getString("Title") }
    alertMessage = view.findViewById<TextView>(R.id.alertMessage).apply { text = args?.getString("Message") }
    alertOKButton = view.findViewById<Button>(R.id.alertOkButton).apply { setOnClickListener { dismiss() } }
    alertCancelButton = view.findViewById<Button>(R.id.alertCancelButton).apply {
      visibility = if (args?.getBoolean("NeedCancelButton")!!) View.VISIBLE else View.INVISIBLE
      setOnClickListener { dismiss() }
    }
  }

  override fun onResume() {
    super.onResume()

    setUpDimensions()
  }

  fun setUpDimensions() {
    val window = dialog!!.window
    val size = Point()

    val display = window!!.windowManager.defaultDisplay
    display.getSize(size)

    val width = size.x
    //val height = size.y

    window.setLayout((width * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT) // 75% of screen width
    //window.setLayout((width * 0.85).toInt(), (height * 0.3).toInt()) // Similar version with height modded as well
    window.setGravity(Gravity.CENTER)
  }

  companion object {
    fun newInstance(title : String, message : String, needCancelButton : Boolean) : AppFragmentAlertDialog {
      val frag = AppFragmentAlertDialog()
      val args = Bundle()
      args.putString("Title", title)
      args.putString("Message", message)
      args.putBoolean("NeedCancelButton", needCancelButton)
      frag.arguments = args
      return frag
    }
  }

}