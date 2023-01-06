package edu.usc.nlcaceres.infectionprevention.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ConstantsTest {
  @Test fun `Check Endpoint Strings Correct`() {
    assertEquals("https://infection-prevention-express.herokuapp.com/api/", BaseURL)
    assertEquals("http://10.0.2.2:3000/api/", BaseDevURL)

    assertEquals("precautions", PrecautionsPath)
    assertEquals("healthpractices", HealthPracticesPath)
    assertEquals("locations", LocationsPath)
    assertEquals("professions", ProfessionsPath)
    assertEquals("employees", EmployeesPath)
    assertEquals("reports", ReportsPath)
    assertEquals("reports/create", ReportCreationPath)
  }

  @Test fun `Check Fragment Transaction Bundle Keys Correctly Templated`() { // Check tricky strings with concatenation
    assertEquals("edu.usc.nlcaceres.infectionprevention", ProjectPkgName)

    assertEquals("edu.usc.nlcaceres.infectionprevention.main", MainFragmentTransaction)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.preselected_filter", PreSelectedFilterExtra)

    assertEquals("edu.usc.nlcaceres.infectionprevention.create_report", CreateReportTransaction)
    assertEquals("edu.usc.nlcaceres.infectionprevention.create_report.health_practice", CreateReportPracticeExtra)
  }

  @Test fun `Check Transition Names Correctly Templated`() {
    assertEquals("edu.usc.nlcaceres.infectionprevention.report_type_textview_transition", ReportTypeTextViewTransition)
  }

  @Test fun `Check Fragment Result Keys and Values Correctly Templated`() {
    // Listeners awaiting result of fragment that just popped
    assertEquals("edu.usc.nlcaceres.infectionprevention.create_report_request", CreateReportRequestKey)

    assertEquals("edu.usc.nlcaceres.infectionprevention.sort_filter_request", SortFilterRequestKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.sort_filter_request.selected_filters", SelectedFilterParcel)

    // Settings' Edit Text Dialog Listener for Preference Updates
    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog", EditTextDialogRequestKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog.preference_key", EditTextDialogPreferenceKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog.preference_value", EditTextDialogPreferenceValue)
  }
}