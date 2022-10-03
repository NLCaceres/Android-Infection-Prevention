package edu.usc.nlcaceres.infectionprevention.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View

// In debug/tests, we shouldn't run animations, so to disable, we make it last only a millisecond
// Why? Tests can get held up by animations. They can even make clicks become longPresses which cause
// tests to be flakey and fail for seemingly no reason

// Could add an onAnimationEnd closure param BUT for this single animation, this implementation is good enough
fun createFlashingAnimation(target: View): ObjectAnimator {
  return ObjectAnimator.ofFloat(target, "alpha", 0.0f, 1.0f).apply {
    duration = 1L // Quickly end animation and set view to GONE, no real interruption to UI tests
    addListener(AnimationEndListener { target.visibility = View.GONE })
  }
}

// Could be more versatile by handling different animation timing callbacks but will build as needed
class AnimationEndListener(private val onAnimationEnd: () -> Unit): AnimatorListenerAdapter() {
  override fun onAnimationEnd(animation: Animator) { onAnimationEnd.invoke() }
}
