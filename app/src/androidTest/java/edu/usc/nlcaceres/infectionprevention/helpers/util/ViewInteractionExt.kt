package edu.usc.nlcaceres.infectionprevention.helpers.util

import android.view.View
import androidx.test.espresso.ViewInteraction
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isFocused
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.clearText
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.endsWith

/* Goal: Make interactions as simple as possible and matchers as reusable and not wordy! */

// Reminder: Basic format of Espresso is onView(someViewMatcher).perform(someViewAction).check(someViewAssertion)
// and for Adapters onData(someObjMatcher).DataOptions.perform(someViewAction).check(someViewAssertion)
// and for Intents intended(someIntentMatcher) OR intending(someIntentMatcher).respondWith(someActivityResult)
// Another Note: 'has' vs 'with'? 'has' seems to match ANY (like a bool, likely best used in check())
// While 'with' seems to match specific (seems best for finding views)

// Matchers - Often used in onView() to find views! ('with' usually)
fun containsText(text: String): Matcher<View> = withText(containsString(text))
fun withPrefix(text: String): Matcher<View> = withText(startsWith(text))
fun withSuffix(text: String): Matcher<View> = withText(endsWith(text))
fun childWithText(text: String): Matcher<View> = hasDescendant(withText(text))
fun childWithTextMatching(matcher: Matcher<String>): Matcher<View> = hasDescendant(withText(matcher))
fun childWithPrefix(text: String): Matcher<View> = childWithTextMatching(startsWith(text))
fun childWithSuffix(text: String): Matcher<View> = childWithTextMatching(endsWith(text))
fun withSibling(id: Int): Matcher<View> = hasSibling(withId(id))
fun withSibling(text: String): Matcher<View> = hasSibling(containsText(text))

// Actions - Starts with perform() on some viewInteraction
fun ViewInteraction.tap(): ViewInteraction = perform(click())
fun tapBackButton(): ViewInteraction = onView(isRoot()).perform(pressBack())
fun ViewInteraction.enterText(text: String): ViewInteraction = perform(typeText(text))
fun ViewInteraction.enterTextIntoFocus(text: String): ViewInteraction = perform(typeTextIntoFocusedView(text))
fun ViewInteraction.updateText(text: String): ViewInteraction = perform(replaceText(text))
fun ViewInteraction.eraseText(): ViewInteraction = perform(clearText())
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
fun ViewInteraction.matching(matcher: Matcher<View>): ViewInteraction = check(matches(matcher))
fun ViewInteraction.hasText(text: String): ViewInteraction = matching(withText(text))
fun ViewInteraction.containingText(text: String): ViewInteraction = matching(containsText(text))
fun ViewInteraction.hasPrefix(text: String): ViewInteraction = matching(withPrefix(text))
fun ViewInteraction.hasSuffix(text: String): ViewInteraction = matching(withSuffix(text))
fun ViewInteraction.hasChildWithText(text: String): ViewInteraction = matching(childWithText(text))
fun ViewInteraction.hasChildWithTextMatching(matcher: Matcher<String>): ViewInteraction = matching(childWithTextMatching(matcher))
fun ViewInteraction.hasChildWithPrefix(text: String): ViewInteraction = matching(childWithPrefix(text))
fun ViewInteraction.hasChildWithSuffix(text: String): ViewInteraction = matching(childWithSuffix(text))
fun ViewInteraction.isTheFocus(): ViewInteraction = matching(isFocused())
fun ViewInteraction.isOnScreen(): ViewInteraction = matching(isDisplayed())
fun ViewInteraction.isHidden(): ViewInteraction = matching(not(isDisplayed()))
fun ViewInteraction.isNotInLayout(): ViewInteraction = check(doesNotExist())