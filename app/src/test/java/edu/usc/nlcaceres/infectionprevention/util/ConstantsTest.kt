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

  @Test fun `Check Intent Strings Correctly Templated`() { // Check tricky strings with concatenation
    assertEquals("edu.usc.nlcaceres.infectionprevention", ProjectPkgName)

    assertEquals("edu.usc.nlcaceres.infectionprevention.main", MainActIntent)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.preselected_filter", PreSelectedFilterExtra)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.precaution_list", PrecautionListExtra)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.health_practice_list", HealthPracticeListExtra)

    assertEquals("edu.usc.nlcaceres.infectionprevention.new_report", CreateReportRequestKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.new_report.health_practice", CreateReportPracticeExtra)

    assertEquals("edu.usc.nlcaceres.infectionprevention.sortFilter", SortFilterRequestKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.sortFilter.filters", SelectedFilterParcel)
  }

  @Test fun `Check Transition Names Correctly Templated`() {
    assertEquals("edu.usc.nlcaceres.infectionprevention.report_type_textview_transition", ReportTypeTextViewTransition)
  }

  @Test fun `Check Fragment Result Keys and Values Correctly Templated`() {
    assertEquals("edu.usc.nlcaceres.infectionprevention.action_view", ActionViewManager)
    assertEquals("edu.usc.nlcaceres.infectionprevention.action_view.closer", ActionViewBundleCloser)

    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog", EditTextDialogManager)
    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog.preference_key", EditTextDialogPreferenceKey)
    assertEquals("edu.usc.nlcaceres.infectionprevention.edit_text_dialog.preference_value", EditTextDialogPreferenceValue)

    assertEquals("edu.usc.nlcaceres.infectionprevention.keyboard", KeyboardManager)
    assertEquals("edu.usc.nlcaceres.infectionprevention.keyboard.closer", KeyboardBundleCloser)

    assertEquals("edu.usc.nlcaceres.infectionprevention.nav_drawer", NavDrawerManager)
    assertEquals("edu.usc.nlcaceres.infectionprevention.nav_drawer.opener", NavDrawerBundleOpener)

    assertEquals("edu.usc.nlcaceres.infectionprevention.snackbar", SnackbarDisplay)
    assertEquals("edu.usc.nlcaceres.infectionprevention.snackbar.message", SnackbarBundleMessage)
  }
}