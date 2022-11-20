package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.FieldNamingPolicy
import edu.usc.nlcaceres.infectionprevention.data.Report

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

// Gson Helper
fun snakeCaseGson(): Gson = GsonBuilder()
  .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
  .registerTypeAdapter(Report::class.java, ReportDeserializer()) // Needs to handle Profession nested in Employee field
  .create() // Catches underscored names e.g. first_name

// Intents/Bundles for sending data across activities/fragments
const val ProjectPkgName = "edu.usc.nlcaceres.infectionprevention"
const val MainActIntent = "$ProjectPkgName.main"
const val PreSelectedFilterExtra = "$MainActIntent.preselected_filter"
const val PrecautionListExtra = "$MainActIntent.precaution_list"
const val HealthPracticeListExtra = "$MainActIntent.health_practice_list"
const val SettingsIntent = "$ProjectPkgName.settings"
const val CreateReportRequestKey = "$ProjectPkgName.new_report"
const val CreateReportPracticeExtra = "$CreateReportRequestKey.health_practice"
const val SortFilterRequestKey = "$ProjectPkgName.sortFilter"
const val SelectedFilterParcel = "$SortFilterRequestKey.filters"

// SharedTransitionNames
const val ReportTypeTextViewTransition = "$ProjectPkgName.report_type_textview_transition"

// FragmentResultListener
const val ActionViewManager = "$ProjectPkgName.action_view"
const val ActionViewBundleCloser = "$ActionViewManager.closer"
const val EditTextDialogManager = "$ProjectPkgName.edit_text_dialog"
const val EditTextDialogPreferenceKey = "$EditTextDialogManager.preference_key"
const val EditTextDialogPreferenceValue = "$EditTextDialogManager.preference_value"
const val KeyboardManager = "$ProjectPkgName.keyboard"
const val KeyboardBundleCloser = "$KeyboardManager.closer"
const val NavDrawerManager = "$ProjectPkgName.nav_drawer"
const val NavDrawerBundleOpener = "$NavDrawerManager.opener"
const val SnackbarDisplay = "$ProjectPkgName.snackbar"
const val SnackbarBundleMessage = "$SnackbarDisplay.message"

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