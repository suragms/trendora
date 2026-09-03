package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.presentation.TrendoraApp

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: TrendoraAppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the branded Android 12+ splash screen during cold start, then
        // transition seamlessly into the Compose UI.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = TrendoraAppContainer(applicationContext)

        setContent {
            TrendoraApp(container = appContainer)
        }
    }
}
