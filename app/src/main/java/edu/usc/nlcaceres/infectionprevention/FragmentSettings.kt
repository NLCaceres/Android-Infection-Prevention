package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.Preference
import androidx.preference.forEach
import edu.usc.nlcaceres.infectionprevention.util.dpUnits
import edu.usc.nlcaceres.infectionprevention.util.setUpIndicator

class FragmentSettings : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { // Called in onCreate
    setPreferencesFromResource(R.xml.preferences, rootKey)
    setupCommonPrefs()
    setupAdminPrefs()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as AppCompatActivity).supportActionBar?.setUpIndicator(R.drawable.ic_back_arrow)
    requireActivity().addMenuProvider(SettingsMenu(), viewLifecycleOwner, Lifecycle.State.RESUMED)

    setDivider(ContextCompat.getDrawable(requireContext(), R.drawable.custom_item_divider)) // Divide Categories
    setDividerHeight(dpUnits(4))
    // Can use insetDrawable w/ listView.addItemDecoration to add dividers between preferences but then
    // ALSO end up with an extra divider between the category title AND its preferences
    childFragmentManager.setFragmentResultListener("EditTextDialog", this, dialogResultListener)
  }
  private inner class SettingsMenu: MenuProvider { // Need inner for fragmentManager
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      menuInflater.inflate(R.menu.action_buttons, menu)
    }
    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
      android.R.id.home -> { parentFragmentManager.popBackStack(); true }
      else -> false
    }
  }

  private fun setupCommonPrefs() { // Preferences all users should see
    findPreference<PreferenceCategory>("user")?.forEach {
      when (it.key) {
        "phone" -> {
          with(it as EditTextPreference) {
            dialogMessage = "Ex: (123) 456-7890"
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
              if (pref.text.isNullOrBlank()) "Please enter your phone number" else pref.text
            }
          }
        }
        "password" -> {
          with(it as EditTextPreference) {
            dialogMessage = "New password"
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
              if (pref.text.isNullOrBlank()) "Unable to find password" else "***${pref.text?.takeLast(2)}"
            }
          }
        }
        else -> { } // If processing is needed for the rest of prefs
      }
    }
  }

  private fun setupAdminPrefs() {
    val fragContext = requireContext()
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(fragContext) // reqContext best for lifecycle methods
    sharedPrefs.getString("username", "")?.let { // Key, defaultVal
      val userIsAdmin = true // TODO: Determine if user == admin
      if (userIsAdmin) {
        val group = EditTextPreference(fragContext).apply {
          key = "group"; title = "Healthcare Group or Clinic Name"
          layoutResource = R.layout.preference_divided
          dialogMessage = "New Name" // Used as editText hint in dialog
          summaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
              if (pref.text.isNullOrBlank()) "Hospital/Clinic Name missing" else pref.text
          }
        }
        val colorHint = "Enter hex color i.e. #FFFFFF"
        val colorSummaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
            if (pref.text.isNullOrBlank()) "Using default color" else pref.text
        }
        val toolbarColor = EditTextPreference(fragContext).apply {
          key = "toolbar"; title = "Toolbar Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val backgroundColor = EditTextPreference(fragContext).apply {
          key = "background"; title = "Background Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val reportTitleColor = EditTextPreference(fragContext).apply {
          key = "report_title"; title = "Report Title Text Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val adminCategory = PreferenceCategory(fragContext).apply {
          key = "admin_cat"; title = "Hospital-wide Admin Settings"
          layoutResource = R.layout.preferences_category
        }
        preferenceManager.preferenceScreen.addPreference(adminCategory)
        adminCategory.addPreference(group)
        adminCategory.addPreference(toolbarColor)
        adminCategory.addPreference(backgroundColor)
        adminCategory.addPreference(reportTitleColor)
      }
    }
  }

  // Ensures customDialog launches for EditTextPrefs
  override fun onDisplayPreferenceDialog(preference: Preference) {
    when (preference) {
      is EditTextPreference -> { // Use dialogMessage prop to set editText hint
        FragmentEditPreferenceDialog.newInstance(preference.key, preference.title as String,
          preference.dialogMessage.toString(), true).show(childFragmentManager, "settings_dialog")
      }
      else -> super.onDisplayPreferenceDialog(preference)
    }
  }
  private val dialogResultListener = FragmentResultListener { requestKey, bundle ->
    when (requestKey) {
      "EditTextDialog" -> { // Get key from pref that finished to update UI with latest text value
        findPreference<EditTextPreference>(bundle.getString("key", ""))?.let {
          it.text = bundle.getString("newVal", "")
        }
      }
    }
  }

  companion object {
    fun newInstance(): FragmentSettings {
      val args = Bundle()
      val fragment = FragmentSettings()
      fragment.arguments = args
      return fragment
    }
  }
}
