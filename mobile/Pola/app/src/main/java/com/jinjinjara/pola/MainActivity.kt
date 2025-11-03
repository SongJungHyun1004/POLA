package com.jinjinjara.pola

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.navigation.PolaNavHost
import com.jinjinjara.pola.presentation.ui.theme.PolaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        enableEdgeToEdge()
        setContent {
            PolaTheme {
                // DataStore의 토큰 존재 여부를 관찰하여 로그인 상태 관리
                val isLoggedIn by authRepository.observeLoginState().collectAsState(initial = false)

                LaunchedEffect(isLoggedIn) {
                    Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
                    val token = authRepository.getAccessToken()
                    Log.d("MainActivity", "Current token: ${token?.take(20)}...")
                }

                PolaNavHost(
                    modifier = Modifier.fillMaxSize(),
                    isLoggedIn = isLoggedIn,
                )
            }
        }
    }
}