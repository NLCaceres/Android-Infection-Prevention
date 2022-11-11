package edu.usc.nlcaceres.infectionprevention.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.helpers.util.tap
import edu.usc.nlcaceres.infectionprevention.helpers.util.hasText
import edu.usc.nlcaceres.infectionprevention.helpers.util.isOnScreen
import edu.usc.nlcaceres.infectionprevention.helpers.util.withDataDescribedAs
import edu.usc.nlcaceres.infectionprevention.helpers.util.matching
import edu.usc.nlcaceres.infectionprevention.helpers.util.hasPrefix
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith

class CreateReportRobot: BaseRobot() {

  fun checkCorrectTitle(text: String) {
    titleTV().hasText(text) // Check title based on report item clicked in mainActivity
  }
  fun openTimeDialog() {
    timeDateET().tap()
    timePicker().isOnScreen()
  }
  fun setTime(hours: Int, minutes: Int) {
    timePicker().perform(PickerActions.setTime(hours, minutes))
  }
  fun setDate(year: Int, month: Int, day: Int) {
    datePicker().isOnScreen()
    datePicker().perform(PickerActions.setDate(year, month, day))
  }
  fun checkTimeDateET(text: String) {
    timeDateET().hasText(text)
  }
  // Spinners
  fun checkSpinnersLoaded() { // Since adapters must use onData to be sure (not onView)
    checkSelectedEmployee("John Smith")
    checkSelectedHealthPractice("Hand Hygiene")
    checkSelectedFacility("USC")
  }
  fun selectEmployee(text: String) {
    onData(withDataDescribedAs<Employee>(text)).tap() // Since withDataDescribedAs is an inline fun,
    // this onData call expands out a bit BUT that's actually pretty common with Espresso funs AND overall this is good "inline" usage!
    // So we get a var directly checking if instanceOf(Employee) thanks to "reified" and another var with startsWith(text)
    // withItemDescribedAs is just called in allOf w/ startsWith, returning a BoundedMatcher w/ our implementation that uses startsWith
    // Along the way, plenty of common/normal notNull checks are performed. Next, an allOf var is made
    // using the is(instanceOf(Employee) and startsWith vars, then allOf is put in onData, and finally tap is called with onData
  }
  fun checkSelectedEmployee(text: String) {
    employeeSpinner().matching(withSpinnerText(startsWith(text)))
  }
  fun selectHealthPractice(text: String) {
    //onData(allOf(thatIs(instanceOf(HealthPractice::class.java)), thatIs(text))).tap()
    onData(withDataDescribedAs<HealthPractice>(text)).tap()
  }
  fun checkSelectedHealthPractice(text: String) {
    healthPracticeSpinner().matching(withSpinnerText(startsWith(text)))
  }
  fun selectFacility(text: String) {
    onData(withDataDescribedAs<Location>(text)).tap()
  }
  fun checkSelectedFacility(text: String) {
    facilitySpinner().matching(withSpinnerText(startsWith(text)))
  }
  fun openEmployeeSpinner() { employeeSpinner().tap() }
  fun openHealthPracticeSpinner() { healthPracticeSpinner().tap() }
  fun openFacilitySpinner() { facilitySpinner().tap() }

  // Common functionality
  fun checkSnackBar() {
    snackbar().hasPrefix("Tap the Time & Date text box")
  }
  fun pressSubmitButton() {
    submitButton().tap()
  }
  fun checkAlertDialog() {
    alertDialog().isOnScreen()
  }
  fun pressCancelButton() {
    dialogCancelButton().tap()
  }
  fun pressOkButton() {
    dialogOkButton().tap()
  }
  fun pressUpButton() {
    onView(withContentDescription("Navigate up")).tap()
  }

  companion object {
    fun titleTV(): ViewInteraction = onView(withId(R.id.headerTV))

    fun timeDateET(): ViewInteraction = onView(withId(R.id.dateEditText))
    fun timePicker(): ViewInteraction = onView(withClassName(equalTo(TimePicker::class.java.name)))
    fun datePicker(): ViewInteraction = onView(withClassName(equalTo(DatePicker::class.java.name)))

    fun employeeSpinner(): ViewInteraction = onView(withId(R.id.employeeSpinner))
    fun healthPracticeSpinner(): ViewInteraction = onView(withId(R.id.healthPracticeSpinner))
    fun facilitySpinner(): ViewInteraction = onView(withId(R.id.facilitySpinner))
    // Can use string resources for decreased flakiness!

    fun snackbar(): ViewInteraction = onView(withId(com.google.android.material.R.id.snackbar_text))
    fun alertDialog(): ViewInteraction = onView(allOf(withId(R.id.parentPanel),
      hasDescendant(withText(R.string.date_alert_dialog_title))))
    // -1 = no ID, R.id.button2 == Cancel, R.id.button1 == OK // isDescendant == within vs withParent == directly within
    fun dialogCancelButton(): ViewInteraction = onView(allOf(withParent(withId(-1)), withText("Cancel")))
    fun dialogOkButton(): ViewInteraction = onView(allOf(withParent(withId(-1)),
      withText(R.string.alert_dialog_ok)))
    fun submitButton(): ViewInteraction = onView(withId(R.id.createReportButton))
  }
}