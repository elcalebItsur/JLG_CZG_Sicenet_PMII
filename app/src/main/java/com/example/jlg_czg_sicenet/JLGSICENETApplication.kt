package com.example.jlg_czg_sicenet

import android.app.Application
import com.example.jlg_czg_sicenet.data.AppContainer
import com.example.jlg_czg_sicenet.data.DefaultAppContainer

class JLGSICENETApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
