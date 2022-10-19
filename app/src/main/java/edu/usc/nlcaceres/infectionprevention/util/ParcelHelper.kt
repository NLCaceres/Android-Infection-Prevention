package edu.usc.nlcaceres.infectionprevention.util

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable

// Handle Android T (SDK33+) deprecation (Can work similarly for Bundle too)
inline fun <reified T : Parcelable> Intent.fetchParcelable(key: String): T? = when {
  SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}
inline fun <reified T : Parcelable> Intent.fetchParcelableList(key: String): ArrayList<T>? = when {
  SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}