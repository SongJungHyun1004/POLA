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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.domain.repository.ChatRepository
import com.jinjinjara.pola.domain.usecase.auth.AutoLoginUseCase
import com.jinjinjara.pola.navigation.PolaNavHost
import com.jinjinjara.pola.presentation.ui.theme.PolaTheme
import com.jinjinjara.pola.util.parcelable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    @Inject
    lateinit var autoLoginUseCase: AutoLoginUseCase

    @Inject
    lateinit var chatRepository: ChatRepository

    private val shareUploadViewModel: ShareUploadViewModel by viewModels()

    // 공유하기로 들어왔는지 확인
    private var isSharedContent = false

    // 공유받은 데이터 저장 (로그인 후 업로드하기 위해)
    private var sharedImageUri: Uri? = null
    private var sharedText: String? = null
    private var sharedContentType: String? = null

    private var hasStartedUpload = false
    private var isAutoLoginCompleted by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")

        // 앱 재시작 시 채팅 메시지 삭제
        lifecycleScope.launch {
            Log.d("MainActivity", "Clearing chat messages on app restart")
            chatRepository.clearAllMessages()
        }

        // 자동 로그인 시도 (백그라운드에서 토큰 검증 및 재발급)
        lifecycleScope.launch {
            Log.d("MainActivity", "Starting auto login")
            val result = autoLoginUseCase()
            Log.d("MainActivity", "Auto login completed: ${if (result is com.jinjinjara.pola.util.Result.Success) "success" else "failed"}")
            isAutoLoginCompleted = true
        }

        // 공유 인텐트인지 확인 및 데이터 추출
        if (intent?.action == Intent.ACTION_SEND) {
            isSharedContent = true
            Log.d("MainActivity", "=== Shared Content Detected ===")

            when {
                // 텍스트 공유
                intent.type?.startsWith("text/") == true -> {
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    Log.d("MainActivity", "Shared text: ${sharedText?.take(50)}...")
                }
                // 이미지 공유
                intent.type?.startsWith("image/") == true -> {
                    sharedImageUri = intent.parcelable(Intent.EXTRA_STREAM)
                    sharedContentType = contentResolver.getType(sharedImageUri!!) ?: "image/png"
                    Log.d("MainActivity", "Shared image URI: $sharedImageUri")
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            PolaTheme {

                // DataStore의 토큰 존재 여부와 온보딩 완료 여부를 관찰하여 상태 관리
                // initial = null로 설정하여 로딩 상태 표시

                val isLoggedIn by authRepository.observeLoginState().collectAsState(initial = null)
                val onboardingCompleted by preferencesDataStore.observeOnboardingCompleted().collectAsState(initial = null)

                LaunchedEffect(isLoggedIn, onboardingCompleted) {
                    Log.d("MainActivity", "State changed - isLoggedIn: $isLoggedIn, onboardingCompleted: $onboardingCompleted")
                    if (isLoggedIn != null) {
                        val token = authRepository.getAccessToken()
                        Log.d("MainActivity", "Current token: ${token ?: "null"}")
                    }
                }

                if (isSharedContent) {
                    // 공유하기로 들어온 경우
                    when (isLoggedIn) {
                        null -> {
                            // 로그인 상태 확인 중
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        false -> {
                            // 로그인 안 됨 → 로그인 화면 표시
                            Log.d("MainActivity", "Not logged in, showing login screen")
                            PolaNavHost(
                                modifier = Modifier.fillMaxSize(),
                                isLoggedIn = false,
                            )

                            // 로그인 완료 감지 후 업로드 시작
                            LaunchedEffect(isLoggedIn) {
                                // 이미 false 상태이므로 여기서는 아무것도 안 함
                            }
                        }
                        true -> {
                            // 로그인 됨 → 업로드 화면 표시
                            val uploadState by shareUploadViewModel.uploadState.collectAsState()

                            // 로그인 되어있으면 바로 업로드 시작
                            LaunchedEffect(Unit) {
                                if (!hasStartedUpload) {  // 플래그 체크
                                    Log.d("MainActivity", "Logged in, starting upload")
                                    hasStartedUpload = true
                                    startUpload()
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                when (val state = uploadState) {
                                    is ShareUploadState.Idle,
                                    is ShareUploadState.Uploading -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(48.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                strokeWidth = 4.dp
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "업로드 중",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "잠시만 기다려주세요",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    is ShareUploadState.Success -> {
                                        LaunchedEffect(Unit) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                state.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            kotlinx.coroutines.delay(500)
                                            finish()
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icons.Default.Check.let { icon ->
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    is ShareUploadState.Error -> {
                                        LaunchedEffect(Unit) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                state.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            kotlinx.coroutines.delay(500)
                                            finish()
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.errorContainer,
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icons.Default.Close.let { icon ->
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "업로드 실패",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }




                } else {
                    // 일반 실행: 기존 로그인 플로우
                    when {
                        !isAutoLoginCompleted || isLoggedIn == null || onboardingCompleted == null -> {
                            // 자동 로그인 미완료 또는 데이터 로딩 중이면 로딩 표시
                            Log.d("MainActivity", "Loading - autoLogin: $isAutoLoginCompleted, isLoggedIn: $isLoggedIn, onboarding: $onboardingCompleted")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        else -> {
                            // 모든 초기화 완료되면 네비게이션 시작
                            Log.d("MainActivity", "Ready to show UI - isLoggedIn: $isLoggedIn, onboarding: $onboardingCompleted")
                            PolaNavHost(
                                modifier = Modifier.fillMaxSize(),
                                isLoggedIn = isLoggedIn ?: false,
                                onboardingCompleted = onboardingCompleted ?: false
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 로그인 화면에서 돌아왔을 때 업로드 시작
        if (isSharedContent && !hasStartedUpload) {
            lifecycleScope.launch {
                val isLoggedIn = authRepository.observeLoginState().first()
                if (isLoggedIn == true) {
                    Log.d("MainActivity", "Logged in after resume, starting upload")
                    hasStartedUpload = true
                    startUpload()
                }
            }
        }
    }

    /**
     * 공유받은 데이터 업로드 시작
     */
    private fun startUpload() {
        when {
            sharedText != null -> {
                shareUploadViewModel.uploadText(sharedText!!)
            }
            sharedImageUri != null && sharedContentType != null -> {
                shareUploadViewModel.uploadImage(sharedImageUri!!, sharedContentType!!)
            }
            else -> {
                Toast.makeText(this, "공유된 데이터를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}