package edu.usc.nlcaceres.infectionprevention.helpers.util

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf

// Useful readable version of Hamcrest's 'is' since it comes up often in onData()
fun <T> thatIs(something: T): Matcher<T> = `is`(something) // EASIER than dealing with kotlin backtick requirement
fun <T> thatIs(someMatcher: Matcher<T>): Matcher<T> = `is`(someMatcher) // Handle cases like "instanceOf" that's a matcher, not a value or type

// Actions
fun DataInteraction.tap(): ViewInteraction = perform(click())

// Custom Matchers - Useful for checking within onData calls for AdapterViews (Spinners, ListView, etc.)
inline fun <reified T> withDataDescribedAs(text: String): Matcher<Any> {
  // Why the complicated fun signature? Couple reasons:
  // 1. A more specific matcher means less tests failing due to Espresso ambiguous reference
  // 2. To make a more specific matcher, we can use instanceOf to check the type of our data stored in our Spinner
  // 3. To get the data type, we need reified to access it at runtime. Reified can only be used on inline funs though!
  // - What do reified + inline do: Reified means the ACTUAL type is provided at the call based on the <Type> contextual info
  //   Inline injects the fun's code at the callsite when compiling rather than looking up vars + creating new memory refs at runtime
  //   To see what this fun becomes when calling, see CreateReportRobot OR go to Tools -> Kotlin -> Show ByteCode -> Decompile
  // CON: Calling inline funs too often can bloat your code (since the generated code is just put there rather than looked up and reused)
  // BUT PRO: Used in moderation and with short funs can improve speed and memory usage immensely!
  return allOf(thatIs(instanceOf(T::class.java)), withItemDescribedAs(text))
}
// Since Spinners and ListViews can commonly just list off Strings
// AND all Kotlin classes derive from Any, which implements 3 funs - equals, hashCode, and most importantly toString!
// By knowing the expected return of toString for any given data (if the AdapterView just contains String types, toString returns the String!)
// We can easily lookup the inner data of our AdapterViews and perform actions on them
fun withItemDescribedAs(text: String): Matcher<Any> {
//   checkNotNull(text) // Doubt necessary in Kotlin BUT common in Java
  return withItemDescribedAs(startsWith(text))
}
fun withItemDescribedAs(matcher: Matcher<String>): Matcher<Any> {
  return object: BoundedMatcher<Any, Any>(Any::class.java) { // Need object since implementing abstract class
    // Two ways to read matcher.matches(): (using startsWith(text) as example)
    // 1. Checks if obj.toString startsWith text
    // 2. StartingWith text, is there a match given the obj's toString return
    override fun matchesSafely(obj: Any): Boolean = matcher.matches(obj.toString())
    override fun describeTo(description: Description) {
      description.appendText("with item description: ")
      matcher.describeTo(description)
    }
  }
}