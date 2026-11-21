package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.data.AppContainer
import com.example.ui.VellumApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = AppContainer(applicationContext)

    setContent {
      VellumApp(
        appContainer = appContainer,
        onDarkThemeChanged = { isDark ->
          val controller = WindowCompat.getInsetsController(window, window.decorView)
          controller.isAppearanceLightStatusBars = !isDark
          controller.isAppearanceLightNavigationBars = !isDark
        },
      )
    }
  }
}
