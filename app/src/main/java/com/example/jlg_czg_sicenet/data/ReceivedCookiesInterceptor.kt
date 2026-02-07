package com.example.jlg_czg_sicenet.data

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class ReceivedCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val cookies = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet("PREF_COOKIES", HashSet()) as HashSet<String>?
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies?.add(header)
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet("PREF_COOKIES", cookies).apply()
        }
        return originalResponse
    }
}
