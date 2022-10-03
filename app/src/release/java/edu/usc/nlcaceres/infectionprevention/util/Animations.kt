package edu.usc.nlcaceres.infectionprevention.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View

fun createFlashingAnimation(target: View): ObjectAnimator {
  return ObjectAnimator.ofFloat(target, "alpha", 0.0f, 1.0f).apply {
    duration = 750L; startDelay = 50 // Animation happens over 750 milliseconds after 50 ms delay
    repeatMode = ValueAnimator.REVERSE // Reverse makes animation go from 1.0 to 0.0 on complete
    repeatCount = 5 // 1st run = 0 to 1, 2nd = 1 to 0, 3rd = 0 to 1, etc.
    // The animation will end w/ alpha = 0, so set to GONE as an optimization step, preventing any future rendering participation
    addListener(AnimationEndListener { target.visibility = View.GONE })
  }
}

class AnimationEndListener(private val onAnimationEnd: () -> Unit): AnimatorListenerAdapter() {
  override fun onAnimationEnd(animation: Animator) { onAnimationEnd.invoke() }
}
