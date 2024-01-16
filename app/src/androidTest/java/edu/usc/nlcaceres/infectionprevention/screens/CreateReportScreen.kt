package edu.usc.nlcaceres.infectionprevention.screens

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.matcher.ViewMatchers
import com.kaspersky.kaspresso.screens.KScreen
import edu.usc.nlcaceres.infectionprevention.FragmentCreateReport
import edu.usc.nlcaceres.infectionprevention.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.picker.date.KDatePicker
import io.github.kakaocup.kakao.picker.time.KTimePicker
import io.github.kakaocup.kakao.spinner.KSpinner
import io.github.kakaocup.kakao.spinner.KSpinnerItem
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KSnackbar
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo

object CreateReportScreen: KScreen<CreateReportScreen>() {
  override val layoutId = R.layout.fragment_create_report
  override val viewClass = FragmentCreateReport::class.java

  val upButton = KButton { withContentDescription("Navigate up") }
  val settingsButton = KButton { withContentDescription("Settings") }

  val headerTV = KTextView { withId(R.id.headerTV) }
  val timePickerET = KEditText { withId(R.id.dateEditText) } // Tap to open up the TimePickerDialog
  val timePicker = KTimePicker { withClassName(equalTo(TimePicker::class.java.name)) }
  val datePicker = KDatePicker { withClassName(equalTo(DatePicker::class.java.name)) } // Opens after TimePicker's OK button is tapped

  val alertDialog = KView { withMatcher(allOf(ViewMatchers.withId(R.id.alertTitle), ViewMatchers.withText(R.string.date_alert_dialog_title))) }
  val dialogCancelButton = KButton { withMatcher(allOf(ViewMatchers.withParent(ViewMatchers.withId(-1)), ViewMatchers.withText("Cancel"))) }
  val dialogOkButton = KButton { withMatcher(allOf(ViewMatchers.withParent(ViewMatchers.withId(-1)), ViewMatchers.withText(R.string.alert_dialog_ok))) }

  val employeeSpinner = KSpinner({ withId(R.id.employeeSpinner) }, { itemType(::KSpinnerItem) })
  val healthPracticeSpinner = KSpinner({ withId(R.id.healthPracticeSpinner) }, { itemType(::KSpinnerItem) })
  val facilitySpinner = KSpinner({ withId(R.id.facilitySpinner) }, { itemType(::KSpinnerItem) })

  val submitButton = KButton { withId(R.id.createReportButton) }

  val snackbar = KSnackbar()
}
