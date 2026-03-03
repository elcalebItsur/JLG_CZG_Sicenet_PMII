package com.example.jlg_czg_sicenet.data

import android.content.Context
import com.example.jlg_czg_sicenet.network.SICENETWService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import com.example.jlg_czg_sicenet.data.local.SNDatabase
import com.example.jlg_czg_sicenet.data.local.SNLocalDao
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

interface AppContainer {
    val snRepository: SNRepository
    val snLocalDao: SNLocalDao
}

class DefaultAppContainer(applicationContext: Context) : AppContainer {
    private val baseUrlSN = "https://sicenet.itsur.edu.mx"
    
    private val database: SNDatabase by lazy {
        SNDatabase.getDatabase(applicationContext)
    }

    override val snLocalDao: SNLocalDao by lazy {
        database.snDao()
    }
    
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val cookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(applicationContext)
        )

        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                if (originalRequest.header("User-Agent") == null) {
                    builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x86) AppleWebKit/537.36")
                }
                if (originalRequest.header("Accept") == null) {
                    builder.header("Accept", "*/*")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(ReceivedCookiesInterceptor(applicationContext))
            .addInterceptor(AddCookiesInterceptor(applicationContext))
            .addInterceptor(logging)
            .build()
    }


    private val snRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
            .baseUrl(baseUrlSN)
            .client(client)
            .build()
    }

    override val snRepository: SNRepository by lazy {
        NetworSNRepository(
            snApiService = snRetrofit.create(SICENETWService::class.java),
            snLocalDao = snLocalDao,
            context = applicationContext
        )
    }
}
