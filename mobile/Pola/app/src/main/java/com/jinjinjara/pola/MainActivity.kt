package com.jinjinjara.pola

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
                // initial = null로 설정하여 로딩 상태 표시
                val isLoggedIn by authRepository.observeLoginState().collectAsState(initial = null)

                LaunchedEffect(isLoggedIn) {
                    Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
                    if (isLoggedIn != null) {
                        val token = authRepository.getAccessToken()
                        Log.d("MainActivity", "Current token: ${token?.take(20) ?: "null"}...")
                    }
                }

                // 로딩 중 (토큰 확인 중)
                when (isLoggedIn) {
                    null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        PolaNavHost(
                            modifier = Modifier.fillMaxSize(),
                            isLoggedIn = isLoggedIn ?: false,
                        )
                    }
                }
            }
        }
    }
}