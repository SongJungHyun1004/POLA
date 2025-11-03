package com.jinjinjara.pola

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
//<<<<<<< HEAD
import com.jinjinjara.pola.domain.repository.AuthRepository
//=======
import com.jinjinjara.pola.navigation.PolaNavHost
//>>>>>>> develop
import com.jinjinjara.pola.presentation.ui.screen.MainScreen
import com.jinjinjara.pola.presentation.ui.screen.start.StartScreen
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
                // 테스트용: 이미지 클릭 시 토큰 없이 메인으로 이동
                var isTestMode by remember { mutableStateOf(false) }

//<<<<<<< HEAD
                LaunchedEffect(isLoggedIn) {
                    Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
                    val token = authRepository.getAccessToken()
                    Log.d("MainActivity", "Current token: ${token?.take(20)}...")
                }

                if (isLoggedIn || isTestMode) {
                    // 로그인 되어있거나 테스트 모드면 메인 화면
                    MainScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 로그인 안 되어있으면 로그인 화면
                    StartScreen(
                        onLoginSuccess = {
                            // 테스트용: 토큰 저장 없이 메인 화면으로 이동
                            isTestMode = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
//=======
//                PolaNavHost(
//                    modifier = Modifier.fillMaxSize(),
//                    isLoggedIn = isLoggedIn,
//                    onLoginSuccess = {
//                        isLoggedIn = true
//                    }
//                )
//
////                if (isLoggedIn) {
////                    // 로그인 되어있으면 메인 화면
////                    MainScreen(
////                        modifier = Modifier.fillMaxSize()
////                    )
////                } else {
////                    // 로그인 안 되어있으면 로그인 화면
////                    StartScreen(
////                        onLoginSuccess = {
////                            isLoggedIn = true
////                        },
////                        modifier = Modifier.fillMaxSize()
////                    )
////                }
//>>>>>>> develop
            }
        }
    }
}