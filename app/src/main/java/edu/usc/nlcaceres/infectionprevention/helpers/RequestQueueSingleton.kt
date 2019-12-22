package edu.usc.nlcaceres.infectionprevention.helpers

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class RequestQueueSingleton constructor(context: Context) {
  companion object {
    @Volatile
    private var INSTANCE: RequestQueueSingleton? = null
    fun getInstance(context: Context) =
        INSTANCE
            ?: synchronized(this) {
          INSTANCE
              ?: RequestQueueSingleton(context).also {
            INSTANCE = it
          }
        }
  }

  val requestQueue : RequestQueue by lazy { // lazy delegation can ONLY be used for VALs
    Volley.newRequestQueue(context.applicationContext) // Creates requestQueue and starts it!
  }
  fun <T> addToRequestQueue(req: Request<T>) {
    requestQueue.add(req)
  }
}