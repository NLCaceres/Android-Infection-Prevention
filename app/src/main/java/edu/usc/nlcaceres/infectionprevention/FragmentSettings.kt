package edu.usc.nlcaceres.infectionprevention

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentResultListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.Preference
import androidx.preference.forEach
import edu.usc.nlcaceres.infectionprevention.util.*

class FragmentSettings : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { // Called in onCreate
    setPreferencesFromResource(R.xml.preferences, rootKey)
    setupCommonPrefs()
    setupAdminPrefs()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setDivider(ContextCompat.getDrawable(requireContext(), R.drawable.custom_item_divider)) // Divide Categories
    setDividerHeight(dpUnits(4))
    // Can use insetDrawable w/ listView.addItemDecoration to add dividers between preferences but then
    // ALSO end up with an extra divider between the category title AND its preferences
    childFragmentManager.setFragmentResultListener(EditTextDialogRequestKey, this, dialogResultListener)
  }

  private fun setupCommonPrefs() { // Preferences all users should see
    findPreference<PreferenceCategory>(PreferenceCategoryUser)?.forEach {
      when (it.key) {
        PreferencePhone -> {
          with(it as EditTextPreference) {
            dialogMessage = "Ex: (123) 456-7890"
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
              if (pref.text.isNullOrBlank()) "Please enter your phone number" else pref.text
            }
          }
        }
        PreferencePassword -> {
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
    sharedPrefs.getString(PreferenceUsername, "")?.let { // Key, defaultVal
      val userIsAdmin = true // TODO: Determine based on username if admin
      if (userIsAdmin) {
        val group = EditTextPreference(fragContext).apply {
          key = PreferenceHospitalGroup; title = "Healthcare Group or Clinic Name"
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
          key = PreferenceToolbarColor; title = "Toolbar Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val backgroundColor = EditTextPreference(fragContext).apply {
          key = PreferenceBackgroundColor; title = "Background Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val reportTitleColor = EditTextPreference(fragContext).apply {
          key = PreferenceReportTitleColor; title = "Report Title Text Color"
          layoutResource = R.layout.preference_divided
          dialogMessage = colorHint
          summaryProvider = colorSummaryProvider
        }
        val adminCategory = PreferenceCategory(fragContext).apply {
          key = PreferenceCategoryAdmin; title = "Hospital-wide Admin Settings"
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
          preference.dialogMessage.toString(), true).show(childFragmentManager, SettingsEditTextPreferenceDialogTag)
      }
      else -> super.onDisplayPreferenceDialog(preference)
    }
  }
  private val dialogResultListener = FragmentResultListener { requestKey, bundle ->
    when (requestKey) {
      EditTextDialogRequestKey -> { // Get key from pref that finished to update UI with latest text value
        findPreference<EditTextPreference>(bundle.getString(EditTextDialogPreferenceKey, ""))?.let {
          it.text = bundle.getString(EditTextDialogPreferenceValue, "")
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
