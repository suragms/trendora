package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.presentation.TrendoraApp

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: TrendoraAppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = TrendoraAppContainer(applicationContext)

        setContent {
            TrendoraApp(container = appContainer)
        }
    }
}
