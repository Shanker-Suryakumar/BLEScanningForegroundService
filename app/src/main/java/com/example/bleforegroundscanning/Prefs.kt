package com.example.bleforegroundscanning

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val APP_PREFS: String = "appPrefs"

    private val preference: SharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)

    var appPrefs: String?
        get() = preference.getString(APP_PREFS, "")
        set(value) = preference.edit().putString(APP_PREFS, value).apply()
}