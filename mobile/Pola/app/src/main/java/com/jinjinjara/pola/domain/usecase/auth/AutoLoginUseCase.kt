package com.jinjinjara.pola.domain.usecase.auth

import android.util.Log
import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.domain.usecase.NoParamsUseCase
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

/**
 * 자동 로그인 UseCase
 *
 * 앱 시작 시 저장된 토큰으로 자동 로그인을 시도합니다.
 *
 * 플로우:
 * 1. Access Token 존재 확인
 * 2. /oauth/verify로 토큰 검증
 * 3. 유효하면 사용자 정보 반환
 * 4. 만료(401)면 /oauth/reissue로 재발급 후 사용자 정보 반환
 * 5. Refresh Token도 만료면 로그아웃 (토큰 삭제)
 */
class AutoLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : NoParamsUseCase<Result<User?>> {

    override suspend fun invoke(): Result<User?> {
        Log.d("Auth:AutoLogin", "=== Auto Login Started ===")

        // Step 1: Access Token 존재 확인
        val accessToken = authRepository.getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            Log.d("Auth:AutoLogin", "No access token found")
            return Result.Success(null)
        }

        Log.d("Auth:AutoLogin", "Access token found: ${accessToken.take(20)}...")

        // Step 2: Access Token 검증
        Log.d("Auth:AutoLogin", "Verifying access token")
        return when (val verifyResult = authRepository.verifyAccessToken()) {
            is Result.Success -> {
                if (verifyResult.data) {
                    // Step 3: 유효한 토큰 → 사용자 정보 가져오기
                    Log.d("Auth:AutoLogin", "Token is valid, getting user info")
                    when (val userResult = authRepository.getUser()) {
                        is Result.Success -> {
                            Log.d("Auth:AutoLogin", "=== Auto Login SUCCESS === User: ${userResult.data.email}")
                            Result.Success(userResult.data)
                        }
                        is Result.Error -> {
                            Log.e("Auth:AutoLogin", "Failed to get user info: ${userResult.message}")
                            userResult
                        }
                        else -> Result.Success(null)
                    }
                } else {
                    Log.d("Auth:AutoLogin", "Token is invalid")
                    Result.Success(null)
                }
            }
            is Result.Error -> {
                // Step 4: 토큰 만료 → Refresh Token으로 재발급 시도
                if (verifyResult.errorType == ErrorType.UNAUTHORIZED) {
                    Log.d("Auth:AutoLogin", "Token expired, trying to reissue")
                    when (val reissueResult = authRepository.reissueAccessToken()) {
                        is Result.Success -> {
                            Log.d("Auth:AutoLogin", "=== Auto Login SUCCESS (after reissue) === User: ${reissueResult.data.email}")
                            Result.Success(reissueResult.data)
                        }
                        is Result.Error -> {
                            // Refresh Token도 만료 → 로그인 필요
                            Log.e("Auth:AutoLogin", "Token reissue failed: ${reissueResult.message}")
                            if (reissueResult.errorType == ErrorType.UNAUTHORIZED) {
                                Log.d("Auth:AutoLogin", "Refresh token expired, need to login")
                                // 토큰 삭제는 reissueAccessToken에서 이미 처리됨
                            }
                            Result.Success(null)
                        }
                        else -> Result.Success(null)
                    }
                } else {
                    Log.e("Auth:AutoLogin", "Token verification failed: ${verifyResult.message}")
                    Result.Success(null)
                }
            }
            else -> Result.Success(null)
        }
    }
}
