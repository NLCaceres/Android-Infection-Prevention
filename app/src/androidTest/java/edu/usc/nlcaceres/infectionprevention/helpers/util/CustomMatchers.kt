package edu.usc.nlcaceres.infectionprevention.helpers.util

import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`

// Useful, bit more readable version of Hamcrest's 'is'
fun <T> thatIs(something: T): Matcher<T> = `is`(something) // EASIER than dealing with kotlin backtick requirement
fun <T> thatIs(someMatcher: Matcher<T>): Matcher<T> = `is`(someMatcher) // Handle cases like "instanceOf" that take a matcher, not a value or type

// Rather than create new classes, create an object that implements a type of interface Matcher
fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
  override fun describeTo(description: Description) {
    description.appendText("ImageView with drawable same as drawable with id $id")
  }

  override fun matchesSafely(view: View): Boolean {
    val context = view.context
    val expectedBitmap = context.getDrawable(id)?.toBitmap()

    return if (view is ImageView) view.drawable.toBitmap().sameAs(expectedBitmap)
      else (view as Button).background.toBitmap().sameAs(expectedBitmap)
  }
}
