package com.example.jlg_czg_sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.jlg_czg_sicenet.ui.JLGSICENETApp
import com.example.jlg_czg_sicenet.ui.theme.JLGSICENETTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JLGSICENETTheme {
                JLGSICENETApp()
            }
        }
    }
}