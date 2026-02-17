package com.example.jlg_czg_sicenet.data

import android.content.Context
import com.example.jlg_czg_sicenet.data.local.LocalRepository
import com.example.jlg_czg_sicenet.data.local.RoomLocalRepository
import com.example.jlg_czg_sicenet.data.local.SicenetDatabase
import com.example.jlg_czg_sicenet.network.SICENETWService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val snRepository: SNRepository
    val localRepository: LocalRepository
}

class DefaultAppContainer(applicationContext: Context) : AppContainer {
    private val baseUrlSN = "https://sicenet.itsur.edu.mx"
    
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        
        val builder = OkHttpClient.Builder()
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
        
        builder
    }

    private val snRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlSN)
            .client(client)
            .build()
    }

    override val snRepository: SNRepository by lazy {
        NetworSNRepository(snRetrofit.create(SICENETWService::class.java))
    }

    override val localRepository: LocalRepository by lazy {
        RoomLocalRepository(SicenetDatabase.getDatabase(applicationContext))
    }
}
