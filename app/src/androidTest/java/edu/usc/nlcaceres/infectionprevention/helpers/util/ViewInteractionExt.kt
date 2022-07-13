package edu.usc.nlcaceres.infectionprevention.helpers.util

import android.view.View
import androidx.test.espresso.ViewInteraction
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.startsWith

/* Goal: Make interactions as simple as possible and matchers as reusable and not wordy! */

// Reminder: Basic format of Espresso is onView(someViewMatcher).perform(someViewAction).check(someViewAssertion)
// and for Adapters onData(someObjMatcher).DataOptions.perform(someViewAction).check(someViewAssertion)
// and for Intents intended(someIntentMatcher) OR intending(someIntentMatcher).respondWith(someActivityResult)
// Another Note: 'has' vs 'with'? 'has' seems to match ANY (like a bool, likely best used in check())
// While 'with' seems to match specific (seems best for finding views)

// Matchers - Often used in onView() to find views! ('with' usually)
fun childWithText(text: String): Matcher<View> = hasDescendant(withText(text))
fun childWithTextMatching(matcher: Matcher<String>): Matcher<View> = hasDescendant(withText(matcher))
fun childWithPrefix(text: String): Matcher<View> = childWithTextMatching(startsWith(text))
fun childWithSuffix(text: String): Matcher<View> = childWithTextMatching(endsWith(text))

// Actions - Starts with perform() on some viewInteraction
fun ViewInteraction.tap(): ViewInteraction = perform(click())
// RecyclerView related (unclear on diff between (scroll/action)To vs (scroll/action)ToHolder) - used VERY similarly)
fun <VH: RecyclerView.ViewHolder> ViewInteraction.swipeTo(childMatcher: Matcher<View>): ViewInteraction =
  perform(scrollTo<VH>(childMatcher)) // Versatile swipeTo
fun <VH: RecyclerView.ViewHolder> ViewInteraction.swipeToLabeled(text: String): ViewInteraction =
  perform(scrollTo<VH>(childWithText(text))) // Commonly used one!
fun <VH: RecyclerView.ViewHolder> ViewInteraction.swipeTo(position: Int): ViewInteraction =
  perform(scrollToPosition<VH>(position)) // Position based swipe
fun <VH: RecyclerView.ViewHolder> ViewInteraction.tapItem(childMatcher: Matcher<View>): ViewInteraction =
  perform(actionOnItem<VH>(childMatcher, click())) // Versatile action
fun <VH: RecyclerView.ViewHolder> ViewInteraction.tapItemLabeled(text: String): ViewInteraction =
  perform(actionOnItem<VH>(childWithText(text), click())) // Commonly used action
fun <VH: RecyclerView.ViewHolder> ViewInteraction.tapOn(position: Int): ViewInteraction =
  perform(actionOnItemAtPosition<VH>(position, click())) // Position based action


// Assertions - starts with check() ('has' or 'is' generally)
fun ViewInteraction.hasText(text: String): ViewInteraction = check(matches(withText(text)))
fun ViewInteraction.hasChildWithText(text: String): ViewInteraction = check(matches(childWithText(text)))
fun ViewInteraction.isOnScreen(): ViewInteraction = check(matches(isDisplayed()))