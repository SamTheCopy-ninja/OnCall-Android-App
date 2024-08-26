package com.example.oncall.adapter

import android.content.Context
import android.content.SharedPreferences

// Object that provides utility methods for working with shared preferences
object PreferenceUtils {
    private const val PREFS_NAME = "MyAppPrefs"
    private const val PREF_NOTIFICATION_SHOWN = "notificationShown"

    // Private function that returns a SharedPreferences instance for the specified context
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Function to set the notification shown state in the shared preferences
    fun setNotificationShown(context: Context, shown: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(PREF_NOTIFICATION_SHOWN, shown)
        editor.apply()
    }

    // Function to retrieves the notification shown state from the shared preferences
    fun isNotificationShown(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOWN, false)
    }
}