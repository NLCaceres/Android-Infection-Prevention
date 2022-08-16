package edu.usc.nlcaceres.infectionprevention.util

import android.view.animation.AlphaAnimation
import android.view.animation.Animation

typealias NullableBlock = (() -> Unit)?

fun createFlashingAnimation(animationStart: NullableBlock = null, animationEnd: NullableBlock = null,
                            animationRepeat: NullableBlock = null): AlphaAnimation {
  return AlphaAnimation(0.0f, 1.0f).apply {
    duration = 750; startOffset = 50; // Animation happens over 750 milliseconds after 50 ms delay
    repeatMode = Animation.REVERSE // Reverse makes animation go from 1.0 to 0.0 on complete
    repeatCount = 5 // 1st run = 0 to 1, 2nd = 1 to 0, 3rd = 0 to 1, etc.
    setAnimationListener(CustomAnimationListener(animationStart, animationEnd, animationRepeat))
  }
}

class CustomAnimationListener(private val animationStart: NullableBlock = null, private val animationEnd: NullableBlock = null,
                        private val animationRepeat: NullableBlock = null) : Animation.AnimationListener {
  override fun onAnimationStart(p0: Animation?) { animationStart?.invoke() }
  override fun onAnimationEnd(p0: Animation?) { animationEnd?.invoke() }
  override fun onAnimationRepeat(p0: Animation?) { animationRepeat?.invoke() }
}
