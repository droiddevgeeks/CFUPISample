package com.cashfree.cashfreetestupi.network

import android.content.Context
import android.net.ConnectivityManager

object NetworkConnectivityUtil {
    fun isNetworkConnected(context: Context?): Boolean {
        return if (context != null) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting
        } else {
            true
        }
    }
}