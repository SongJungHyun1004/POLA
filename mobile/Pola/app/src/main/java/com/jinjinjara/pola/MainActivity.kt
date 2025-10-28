package com.jinjinjara.pola

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jinjinjara.pola.presentation.ui.screen.MainScreen
import com.jinjinjara.pola.presentation.ui.screen.start.StartScreen
import com.jinjinjara.pola.presentation.ui.theme.PolaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolaTheme {
                // 로그인 상태 관리 (실제로는 DataStore나 ViewModel에서 관리)
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    // 로그인 되어있으면 메인 화면
                    MainScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 로그인 안 되어있으면 로그인 화면
                    StartScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}