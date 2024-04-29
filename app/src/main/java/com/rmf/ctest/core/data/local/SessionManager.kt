package com.rmf.ctest.core.data.local

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    app: Application
) {

    companion object {
        const val EMAIL = "email"
        const val LOGIN_WITH = "login_with"
    }

    private var pref: SharedPreferences =
        app.getSharedPreferences("ctest", Context.MODE_PRIVATE)

    @SuppressLint("CommitPrefEdits")
    fun updateEmail(email: String) {
        val editor = pref.edit()
        editor.putString(EMAIL, email)
        editor.apply()
    }

    fun getEmail(): String? {
        return pref.getString(EMAIL, "")
    }

    @SuppressLint("CommitPrefEdits")
    fun updateLoginWith(value: String) {
        val editor = pref.edit()
        editor.putString(LOGIN_WITH, value)
        editor.apply()
    }

    fun getLoginWith(): String? {
        return pref.getString(LOGIN_WITH, "")
    }

    fun clear() {
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }

}