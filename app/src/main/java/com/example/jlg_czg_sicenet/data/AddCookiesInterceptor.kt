package com.example.jlg_czg_sicenet.data

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet("PREF_COOKIES", HashSet()) as HashSet<String>?
        // Agregar cada cookie como header
        for (cookie in preferences!!) {
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build())
    }
}
