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
import com.jinjinjara.pola.util.ErrorType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Google 로그인 결과 데이터
data class GoogleSignInResult(
    val idToken: String,
    val displayName: String
)

// Google 로그인 매니저
// Credential Manager를 사용하여 Google ID Token 발급
@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    companion object {
        private const val TAG = "GoogleSignInManager"
        private val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID
    }

    // 구글 로그인 시작
    // context: Activity context 필수
    // onSuccess: 성공 시 GoogleSignInResult 반환
    // onError: 실패 시 에러 메시지 및 에러 타입 반환
    suspend fun signIn(
        context: Context,
        onSuccess: suspend (GoogleSignInResult) -> Unit,
        onError: suspend (String, ErrorType) -> Unit
    ) {
        try {
            Log.d("Auth:Google", "=== Google Sign-In Started ===")
            Log.d("Auth:Google", "Client ID: ${WEB_CLIENT_ID.take(20)}...${WEB_CLIENT_ID.takeLast(10)}")
            Log.d("Auth:Google", "Context: ${context.javaClass.simpleName}")
            Log.d("Auth:Google", "Package: ${context.packageName}")

            val credentialManager = CredentialManager.create(context)

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            Log.d("Auth:Google", "Package info: ${packageInfo.packageName}")
            Log.d("Auth:Google", "CredentialManager available: ${credentialManager != null}")


            // 계정 선택 UI 표시 설정
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            Log.d("Auth:Google", "Requesting credentials from Google...")
            val result = credentialManager.getCredential(context, request)
            Log.d("Auth:Google", "Credentials received from Google")

            val signInResult = handleSignInResult(result)
            if (signInResult != null) {
                Log.d("Auth:Google", "=== Google Sign-In SUCCESS === Name: ${signInResult.displayName}")
                onSuccess(signInResult)
            } else {
                Log.e("Auth:Google", "=== Google Sign-In FAILED === Cannot extract credentials")
                onError("로그인 정보를 가져올 수 없습니다.", ErrorType.GOOGLE_SIGN_IN_FAILED)
            }

        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth:Google", "=== Google Sign-In CANCELLED === User cancelled")
            onError("로그인이 취소되었습니다.", ErrorType.GOOGLE_SIGN_IN_CANCELLED)
        } catch (e: NoCredentialException) {
            Log.e("Auth:Google", "=== Google Sign-In FAILED === No credentials available", e)
            onError("등록된 구글 계정이 없습니다.", ErrorType.GOOGLE_SIGN_IN_FAILED)
        } catch (e: GetCredentialException) {
            Log.e("Auth:Google", "=== Google Sign-In FAILED === Credential error: ${e.type}", e)
            onError("구글 로그인에 실패했습니다.", ErrorType.GOOGLE_SIGN_IN_FAILED)
        } catch (e: Exception) {
            Log.e("Auth:Google", "=== Google Sign-In FAILED === Unknown error", e)
            onError("로그인 중 오류가 발생했습니다: ${e.message}", ErrorType.UNKNOWN)
        }
    }

    // Credential Manager 응답 처리
    private fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult? {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d("Auth:Google", "Google ID token parsed successfully")
                        Log.d("Auth:Google", "ID Token: ${googleIdTokenCredential.idToken}")
                        GoogleSignInResult(
                            idToken = googleIdTokenCredential.idToken,
                            displayName = googleIdTokenCredential.displayName ?: "사용자"
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("Auth:Google", "Failed to parse Google ID token", e)
                        null
                    }
                } else {
                    Log.e("Auth:Google", "Unexpected credential type: ${credential.type}")
                    null
                }
            }
            else -> {
                Log.e("Auth:Google", "Unknown credential type")
                null
            }
        }
    }

    // Nonce 생성하여 보안 강화
    private fun generateNonce(): String {
        val random = UUID.randomUUID().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(random.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
