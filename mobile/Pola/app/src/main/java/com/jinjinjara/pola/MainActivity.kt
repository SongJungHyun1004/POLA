package com.jinjinjara.pola

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.jinjinjara.pola.util.parcelable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val shareUploadViewModel: ShareUploadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        enableEdgeToEdge()

        setContent {
            PolaTheme {
                // DataStore의 토큰 존재 여부를 관찰하여 로그인 상태 관리
                val isLoggedIn by authRepository.observeLoginState().collectAsState(initial = null)

                // 공유 업로드 상태 관찰
                val uploadState by shareUploadViewModel.uploadState.collectAsState()

                // 인증 상태 로깅
                LaunchedEffect(isLoggedIn) {
                    Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
                    if (isLoggedIn != null) {
                        val token = authRepository.getAccessToken()
                        Log.d("MainActivity", "Current token: ${token?.take(20) ?: "null"}...")
                    }
                }

                // 업로드 상태 처리
                LaunchedEffect(uploadState) {
                    when (val state = uploadState) {
                        is ShareUploadState.Uploading -> {
                            Toast.makeText(
                                this@MainActivity,
                                "📤 업로드 중...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is ShareUploadState.Success -> {
                            Toast.makeText(
                                this@MainActivity,
                                "✅ ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareUploadViewModel.resetState()
                        }
                        is ShareUploadState.Error -> {
                            Toast.makeText(
                                this@MainActivity,
                                "❌ ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareUploadViewModel.resetState()
                        }
                        else -> {}
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

        // 공유 인텐트 처리
        checkAndHandleSharedContent()
    }

    /**
     * 공유로 들어온 데이터 확인 및 처리
     */
    private fun checkAndHandleSharedContent() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("MainActivity", "=== Shared Content Detected ===")

                when {
                    // 텍스트 공유
                    intent.type?.startsWith("text/") == true -> {
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        if (text != null) {
                            Log.d("MainActivity", "Shared text: ${text.take(50)}...")
                            shareUploadViewModel.uploadText(text)
                        } else {
                            Toast.makeText(this, "텍스트를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 이미지 공유
                    intent.type?.startsWith("image/") == true -> {
                        val imageUri: Uri? = intent.parcelable(Intent.EXTRA_STREAM)
                        if (imageUri != null) {
                            Log.d("MainActivity", "Shared image URI: $imageUri")
                            val contentType = contentResolver.getType(imageUri) ?: "image/png"
                            shareUploadViewModel.uploadImage(imageUri, contentType)
                        } else {
                            Toast.makeText(this, "이미지를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        Log.w("MainActivity", "Unsupported share type: ${intent.type}")
                        Toast.makeText(this, "지원하지 않는 형식입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                Log.d("MainActivity", "Normal app launch (not shared content)")
            }
        }
    }
}