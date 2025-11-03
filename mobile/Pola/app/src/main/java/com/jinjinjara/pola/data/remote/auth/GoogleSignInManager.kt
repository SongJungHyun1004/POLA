package com.jinjinjara.pola.data.remote.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jinjinjara.pola.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  Google 로그인 매니저 (Credential Manager 기반)
 * - Google ID Token을 발급받는 역할만 담당
 * - 이후 AuthRepositoryImpl에서 서버로 전달하여 로그인 처리
 */
@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    companion object {
        private const val TAG = "GoogleSignInManager"
        private val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID
    }

    /**
     * 구글 로그인 시작
     * @param context - Activity context (필수)
     * @param onSuccess - 성공 시 ID Token 반환
     * @param onError - 실패 시 에러 메시지 반환
     */
    suspend fun signIn(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "signIn() started")
            Log.d(TAG, "WEB_CLIENT_ID: ${WEB_CLIENT_ID.take(20)}...${WEB_CLIENT_ID.takeLast(10)}")
            Log.d(TAG, "Context type: ${context.javaClass.simpleName}")
            Log.d(TAG, "Package name: ${context.packageName}")

            val credentialManager = CredentialManager.create(context)

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            Log.d(TAG, "Using package: ${packageInfo.packageName}")
            Log.d(TAG, "CredentialManager available: ${CredentialManager.create(context) != null}")


            // 직접 계정 선택 UI를 표시 (filterByAuthorizedAccounts = false)
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)  // 🔥 모든 계정 표시
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)  // 자동 선택 비활성화 -> 무조건 UI 표시
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            Log.d(TAG, "Requesting credentials...")
            val result = credentialManager.getCredential(context, request)
            Log.d(TAG, "Credential received")

            val idToken = handleSignInResult(result)
            if (idToken != null) {
                Log.d(TAG, "ID Token extracted successfully")
                onSuccess(idToken)
            } else {
                Log.e(TAG, "Failed to extract ID Token from credential")
                onError("ID Token을 가져올 수 없습니다.")
            }

        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, "User cancelled sign in", e)
            onError("로그인이 취소되었습니다.")
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credential available - Details: ${e.message}", e)
            onError("구글 로그인을 사용할 수 없습니다.\n\n가능한 원인:\n- 기기에 구글 계정이 없음\n- Google Cloud Console SHA-1 미등록\n- Google Play Services 버전 낮음")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.type} - ${e.message}", e)
            onError("로그인 실패: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Google login failed: ${e::class.java.simpleName} - ${e.message}", e)
            onError("알 수 없는 오류: ${e.message}")
        }
    }

    /** Credential Manager 응답 처리 */
    private fun handleSignInResult(result: GetCredentialResponse): String? {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                        null
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    null
                }
            }
            else -> {
                Log.e(TAG, "Unknown credential type")
                null
            }
        }
    }

    /** Nonce(난수 해시) 생성 → 보안 강화 */
    private fun generateNonce(): String {
        val random = UUID.randomUUID().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(random.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
