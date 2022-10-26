package edu.usc.nlcaceres.infectionprevention.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ConstantsTest {
  @Test fun endpointStrings() { // May drop due to Retrofit usage (along with Volley constants)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/", baseURL)
    assertEquals("http://10.0.2.2:3000/api/", baseDevURL)

    assertEquals("https://infection-prevention-express.herokuapp.com/api/precautions", precautionsURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/healthpractices", practicesURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/locations", locationsURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/professions", professionsURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/employees", employeesURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/reports", reportsURL)
    assertEquals("https://infection-prevention-express.herokuapp.com/api/reports/create", reportCreationURL)

    assertEquals("http://10.0.2.2:3000/api/precautions", precautionsDevURL)
    assertEquals("http://10.0.2.2:3000/api/healthpractices", practicesDevURL)
    assertEquals("http://10.0.2.2:3000/api/locations", locationsDevURL)
    assertEquals("http://10.0.2.2:3000/api/professions", professionsDevURL)
    assertEquals("http://10.0.2.2:3000/api/employees", employeesDevURL)
    assertEquals("http://10.0.2.2:3000/api/reports", reportsDevURL)
    assertEquals("http://10.0.2.2:3000/api/reports/create", reportCreationDevURL)
  }

  @Test fun intentStrings() { // Check tricky strings with concatenation
    assertEquals("edu.usc.nlcaceres.infectionprevention", ProjectPkgName)

    assertEquals("edu.usc.nlcaceres.infectionprevention.main", MainActIntent)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.preselected_filter", PreSelectedFilterExtra)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.precaution_list", PrecautionListExtra)
    assertEquals("edu.usc.nlcaceres.infectionprevention.main.health_practice_list", HealthPracticeListExtra)

    assertEquals("edu.usc.nlcaceres.infectionprevention.new_report", CreateReportIntent)
    assertEquals("edu.usc.nlcaceres.infectionprevention.new_report.health_practice", CreateReportPracticeExtra)

    assertEquals("edu.usc.nlcaceres.infectionprevention.sortFilter", SortFilterIntent)
    assertEquals("edu.usc.nlcaceres.infectionprevention.sortFilter.filters", SelectedFilterParcel)
  }

  @Test fun fragmentResultKeys() {
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