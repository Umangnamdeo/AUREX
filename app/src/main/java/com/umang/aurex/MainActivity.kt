package com.umang.aurex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umang.aurex.ui.AurexRoot
import com.umang.aurex.ui.AurexViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { p: SplashScreenViewProvider ->
            p.view.animate().alpha(0f).scaleX(1.18f).scaleY(1.18f).setDuration(420)
                .withEndAction { p.remove() }.start()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AurexViewModel = viewModel()
            AurexRoot(vm)
        }
    }
}
