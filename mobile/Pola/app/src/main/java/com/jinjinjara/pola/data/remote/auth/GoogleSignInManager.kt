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
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "GoogleSignInManager"
        private val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID
    }

    /**
     * 구글 로그인 시작
     * @param onSuccess - 성공 시 ID Token 반환
     * @param onError - 실패 시 에러 메시지 반환
     */
    suspend fun signIn(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val saved = trySignInWithSavedAccount()
            if (saved != null) {
                onSuccess(saved)
                return
            }

            val newToken = signInWithNewAccount()
            onSuccess(newToken)

        } catch (e: GetCredentialCancellationException) {
            onError("로그인이 취소되었습니다.")
        } catch (e: NoCredentialException) {
            onError("저장된 계정이 없습니다.")
        } catch (e: GetCredentialException) {
            onError("로그인 실패: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Google login failed: ${e::class.java.simpleName} - ${e.message}", e)
            onError("알 수 없는 오류: ${e.message}")
        }
    }

    /** 저장된 구글 계정으로 로그인 (One Tap) */
    private suspend fun trySignInWithSavedAccount(): String? {
        return try {
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (_: NoCredentialException) {
            null
        }
    }

    /** 새 계정으로 로그인 */
    private suspend fun signInWithNewAccount(): String {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setNonce(generateNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = credentialManager.getCredential(context, request)
        return handleSignInResult(result)
            ?: throw Exception("ID Token을 가져올 수 없습니다.")
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
