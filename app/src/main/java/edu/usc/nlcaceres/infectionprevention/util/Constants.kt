package edu.usc.nlcaceres.infectionprevention.util

// Backend Endpoints - Base URLS need to end with '/' for Retrofit!
const val BaseURL = "https://infection-prevention-express.herokuapp.com/api/"
const val BaseDevURL = "http://10.0.2.2:8080/api/" // LocalHost if connecting to locally spun up dev server
const val ReportsPath = "reports"
const val ReportCreationPath = "reports/create"
const val EmployeesPath = "employees"
const val HealthPracticesPath = "healthPractices" // The backend is case-insensitive BUT camelCase is more readable
const val LocationsPath = "locations"
const val PrecautionsPath = "precautions"
const val ProfessionsPath = "professions"

// Navigation Argument Extras for sending data across fragments
const val ProjectPkgName = "edu.usc.nlcaceres.infectionprevention"
const val ShortcutIntentAction = "android.intent.action.VIEW" // May use an identifier BUT only available in API 29+
const val MainFragmentTransaction = "$ProjectPkgName.main" // Transaction from Main to ReportList view
const val PreSelectedFilterExtra = "$MainFragmentTransaction.preselected_filter"
const val CreateReportTransaction = "$ProjectPkgName.create_report"
const val CreateReportPracticeExtra = "$CreateReportTransaction.health_practice"
const val NavDrawerCloseNavArgKey = "$ProjectPkgName.nav_drawer.close_drawer"

// SharedTransitionNames
const val ReportTypeTextViewTransition = "$ProjectPkgName.report_type_textview_transition"

// FragmentResultListeners
// Following Requests signal to previous fragments' resultListeners that the current fragment completed its job
const val CreateReportRequestKey = "$ProjectPkgName.create_report_request" // Report was submitted, go to ReportList
const val SortFilterRequestKey = "$ProjectPkgName.sort_filter_request" // Filters selected, begin sorting and filtering
const val SelectedFilterParcel = "$SortFilterRequestKey.selected_filters"
// Following handles updating Preferences when changed in Settings' EditTextDialogs
const val EditTextDialogRequestKey = "$ProjectPkgName.edit_text_dialog"
const val EditTextDialogPreferenceKey = "$EditTextDialogRequestKey.preference_key" // Gets key from bundle for findPref() to get PrefView
const val EditTextDialogPreferenceValue = "$EditTextDialogRequestKey.preference_value" // Gets value from bundle to update UI

// CustomAlertDialog Keys + Tags
const val CreateReportAlertDialogTag = "$ProjectPkgName.createReport_missing_date_alert_dialog"
const val SettingsEditTextPreferenceDialogTag = "$ProjectPkgName.settings_editText_preference_dialog"
const val CustomAlertDialogBundle = "$ProjectPkgName.custom_alert_dialog"
const val AlertDialogBundleTitleKey = "$CustomAlertDialogBundle.title" // Used to get title from bundle to set a title textview
const val AlertDialogBundleMessageKey = "$CustomAlertDialogBundle.message" // Used to get message from bundle to set a message textview
const val AlertDialogBundleHintKey = "$CustomAlertDialogBundle.hint" // Used to get hint from bundle to set hint in a editText
const val AlertDialogBundleOkButtonListenerKey = "$CustomAlertDialogBundle.ok_button_listener"
const val AlertDialogBundleNeedBasicCancelButtonKey = "$CustomAlertDialogBundle.need_basic_cancel_button"
const val AlertDialogBundleCancelButtonListenerKey = "$CustomAlertDialogBundle.cancel_button_listener"

// Settings Preferences
const val PreferenceCategoryUser = "user_category"
const val PreferenceUsername = "username"
const val PreferencePhone = "phone"
const val PreferencePassword = "password"
const val PreferenceCategoryAdmin = "admin_category"
const val PreferenceHospitalGroup = "group"
const val PreferenceToolbarColor = "toolbar"
const val PreferenceBackgroundColor = "background"
const val PreferenceReportTitleColor = "report_title"