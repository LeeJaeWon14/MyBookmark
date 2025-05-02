package com.example.opengraphsample.util

import android.content.Context
import android.content.SharedPreferences

class Pref(private val context : Context) {
    private val PREF_NAME = "PrefOfLee"
    private var preference : SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_DATA = "Preference Test"
        const val USE_EXT_BROWSER = "External browser use"
        const val USE_CLIP_DATA = "USE_CLIP_DATA"

        private var instance : Pref? =null
        @Synchronized
        fun getInstance(context: Context) : Pref? {
            if(instance == null)
                instance = Pref(context)

            return instance
        }
    }

    fun getString(id: String?) : String? {
        return preference.getString(id, "")
    }

    fun getBoolean(id: String?) : Boolean {
        return preference.getBoolean(id, false)
    }

    fun setValue(id: String?, value: String) : Boolean {
        return preference.edit()
            .putString(id, value)
            .commit()
    }

    fun setValue(id: String?, value: Boolean) : Boolean {
        return preference.edit()
                .putBoolean(id, value)
                .commit()
    }

    fun removeValue(id: String?) : Boolean {
        return preference.edit()
            .remove(id)
            .commit()
    }
}