package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 Repository 인터페이스
 */
interface AuthRepository {

    /**
     * 로그인
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인된 사용자 정보
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * 회원가입
     * @param email 이메일
     * @param password 비밀번호
     * @param name 이름
     * @return 생성된 사용자 정보
     */
    suspend fun signUp(email: String, password: String, name: String): Result<User>

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Unit>

    /**
     * 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    suspend fun refreshToken(refreshToken: String): Result<String>

    /**
     * 현재 로그인 상태 확인
     * @return 로그인 여부
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 로그인 상태를 Flow로 관찰
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
     * 현재 사용자 정보 가져오기
     */
    suspend fun getCurrentUser(): Result<User>
}