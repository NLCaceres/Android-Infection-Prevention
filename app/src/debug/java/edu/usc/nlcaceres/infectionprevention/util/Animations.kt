package edu.usc.nlcaceres.infectionprevention.util

import android.view.animation.AlphaAnimation
import android.view.animation.Animation

typealias NullableBlock = (() -> Unit)?

// In debug/tests, we shouldn't run animations, so to disable, we make it last only a millisecond
// Why? Tests can get held up by animations. They can even make clicks become longPresses which cause
// tests to be flakey and fail for seemingly no reason
fun createFlashingAnimation(animationStart: NullableBlock = null, animationEnd: NullableBlock = null,
                            animationRepeat: NullableBlock = null): AlphaAnimation {
  return AlphaAnimation(0.0f, 1.0f).apply {
    duration = 1
    setAnimationListener(CustomAnimationListener(animationStart, animationEnd, animationRepeat))
  }
}

// This way the animation listener be as versatile as possible, we decide what happens on each trigger
class CustomAnimationListener(private val animationStart: NullableBlock = null, private val animationEnd: NullableBlock = null,
                        private val animationRepeat: NullableBlock = null) : Animation.AnimationListener {
  override fun onAnimationStart(p0: Animation?) { animationStart?.invoke() }
  override fun onAnimationEnd(p0: Animation?) { animationEnd?.invoke() }
  override fun onAnimationRepeat(p0: Animation?) { animationRepeat?.invoke() }
}
