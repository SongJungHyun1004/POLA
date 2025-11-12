package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 Repository 인터페이스
 */
interface AuthRepository {

    /**
     * OAuth 2.0 Google 로그인
     * @param idToken Google ID Token
     * @param displayName Google 계정 이름
     * @return 로그인된 사용자 정보
     */
    suspend fun googleLoginWithOAuth(idToken: String, displayName: String): Result<User>

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Unit>

    /**
     * 로그인 상태 확인
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 로그인 상태 관찰
     */
    fun observeLoginState(): Flow<Boolean>

    /**
     * 액세스 토큰 가져오기
     */
    suspend fun getAccessToken(): String?

    /**
     * 리프레시 토큰 가져오기
     */
    suspend fun getRefreshToken(): String?

    /**
     * 토큰 저장
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /**
     * 토큰 삭제 (로그아웃 시)
     */
    suspend fun clearTokens()

    /**
     * 사용자 정보 가져오기
     */
    suspend fun getUser(): Result<User>

    /**
     * Access Token 유효성 검증
     * @return 검증 성공 시 true, 실패 시 false
     */
    suspend fun verifyAccessToken(): Result<Boolean>

    /**
     * Access Token 재발급
     * @return 재발급된 사용자 정보
     */
    suspend fun reissueAccessToken(): Result<User>
}
