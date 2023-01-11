package edu.usc.nlcaceres.infectionprevention.util

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

// Handle Android T (SDK33+) deprecation (ArrayList pre-33 remains not truly type safe. Rest are fine!)
inline fun <reified T : Parcelable> Intent.fetchParcelable(key: String): T? = when {
  SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}
inline fun <reified T : Parcelable> Bundle.fetchParcelable(key: String): T? = when {
  SDK_INT >= 33 -> getParcelable(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
inline fun <reified T : Parcelable> Intent.fetchParcelableList(key: String): ArrayList<T>? = when {
  SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}
inline fun <reified T : Parcelable> Bundle.fetchParcelableList(key: String): ArrayList<T>? = when {
  SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
  else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

// Serializable Version for Bundle
inline fun <reified T : Serializable> Bundle.fetchSerializable(key: String): T? = when {
  SDK_INT >= 33 -> getSerializable(key, T::class.java)
  else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}