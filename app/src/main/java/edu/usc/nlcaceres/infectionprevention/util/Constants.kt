package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder

// Backend Endpoints
const val baseURL = "https://infection-prevention-express.herokuapp.com/api/"
const val baseDevURL = "http://10.0.2.2:3000/api/" // LocalHost
const val precautionsURL = "$baseURL/precautions"
const val precautionsDevURL = "$baseDevURL/precautions"
const val practicesURL = "$baseURL/healthpractices"
const val practicesDevURL = "$baseDevURL/healthpractices"
const val locationsURL = "$baseURL/locations"
const val locationsDevURL = "$baseDevURL/locations"
const val professionsURL = "$baseURL/professions"
const val professionsDevURL = "$baseDevURL/professions"
const val employeesURL = "$baseURL/employees"
const val employeesDevURL = "$baseDevURL/employees"
const val reportsURL = "$baseURL/reports"
const val reportsDevURL = "$baseDevURL/reports"
const val reportCreationURL = "$baseURL/reports/create"
const val reportCreationDevURL = "$baseDevURL/reports/create"

// Gson Helper
fun snakeCaseGson(): Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
  create() // Catches underscored names e.g. first_name

// Volley
const val TIMEOUT_MS = 5000
const val MAX_RETRIES = 3
const val BACKOFF_MULTIPLIER = 1f // Used by Volley to make progressively longer intervals between retry requests!
// Volley Cancel Tags
const val VolleyRequestCancelTag = "CancelTag"
const val MainActivityRequestCancelTag = "$VolleyRequestCancelTag.MainActivityRequest"
const val CreateReportFragCancelTag = "$VolleyRequestCancelTag.CreateReportFragmentRequest"
const val ReportListCancelTag = "$VolleyRequestCancelTag.ReportFragmentRequest"

// Intents
const val mainActIntent = "edu.usc.nlcaceres.infectionprevention.main"
const val preSelectedFilterExtra = "$mainActIntent.preselected_filter"
const val settingsIntent = "edu.usc.nlcaceres.infectionprevention.settings"
const val createReportIntent = "edu.usc.nlcaceres.infectionprevention.new_report"
const val createReportPracticeExtra = "$createReportIntent.health_practice"
const val sortFilterIntent = "edu.usc.nlcaceres.infectionprevention.sortFilter"
const val selectedFilterParcel = "$sortFilterIntent.filters"

// Activity Result Request Codes
const val CreateReportRequestCode = 36
const val ReportListRequestCode = 24