package edu.usc.nlcaceres.infectionprevention.util

// Backend Endpoints - Base URLS need to end with '/' for Retrofit!
const val BaseURL = "https://infection-prevention-express.herokuapp.com/api/"
const val BaseDevURL = "http://10.0.2.2:3000/api/" // LocalHost if connecting to locally spun up dev server
const val ReportsPath = "reports"
const val ReportCreationPath = "reports/create"
const val EmployeesPath = "employees"
const val HealthPracticesPath = "healthpractices"
const val LocationsPath = "locations"
const val PrecautionsPath = "precautions"
const val ProfessionsPath = "professions"

// Intents'/Transactions' Extras for sending data across activities/fragments
const val ProjectPkgName = "edu.usc.nlcaceres.infectionprevention"
const val ShortcutIntentAction = "android.intent.action.VIEW" // May use an identifier BUT only available in API 29+
const val MainFragmentTransaction = "$ProjectPkgName.main" // Transaction from Main to ReportList view
const val PreSelectedFilterExtra = "$MainFragmentTransaction.preselected_filter"
const val PrecautionListExtra = "$MainFragmentTransaction.precaution_list"
const val HealthPracticeListExtra = "$MainFragmentTransaction.health_practice_list"
const val CreateReportTransaction = "$ProjectPkgName.create_report"
const val CreateReportPracticeExtra = "$CreateReportTransaction.health_practice"

// SharedTransitionNames
const val ReportTypeTextViewTransition = "$ProjectPkgName.report_type_textview_transition"

// FragmentResultListeners
// ActivityMain's Listener that runs common app functionality like displaying the Keyboard & Snackbar
const val ActionViewRequestKey = "$ProjectPkgName.action_view"
const val ActionViewIsClosingParcel = "$ActionViewRequestKey.closer"
const val KeyboardRequestKey = "$ProjectPkgName.keyboard"
const val KeyboardIsClosingParcel = "$KeyboardRequestKey.closer"
const val NavDrawerRequestKey = "$ProjectPkgName.nav_drawer"
const val NavDrawerIsOpeningParcel = "$NavDrawerRequestKey.opener"
const val SnackbarRequestKey = "$ProjectPkgName.snackbar"
const val SnackbarMessageParcel = "$SnackbarRequestKey.message"
// Following Requests signal to previous fragments' resultListeners that the current fragment completed its job
const val CreateReportRequestKey = "$ProjectPkgName.create_report_request" // Report was submitted, go to ReportList
const val SortFilterRequestKey = "$ProjectPkgName.sort_filter_request" // Filters selected, begin sorting and filtering
const val SelectedFilterParcel = "$SortFilterRequestKey.selected_filters"
// Following handles updating Preferences when changed in Settings' EditTextDialogs
const val EditTextDialogRequestKey = "$ProjectPkgName.edit_text_dialog"
const val EditTextDialogPreferenceKey = "$EditTextDialogRequestKey.preference_key" // Gets key from bundle for findPref() to get PrefView
const val EditTextDialogPreferenceValue = "$EditTextDialogRequestKey.preference_value" // Gets value from bundle to update UI

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