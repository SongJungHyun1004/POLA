package com.jinjinjara.pola.domain.usecase.auth

import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

// 로그아웃 UseCase
// 서버 로그아웃 요청 후 로컬 토큰 및 사용자 정보 삭제
// 온보딩 플래그는 유지
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesDataStore: PreferencesDataStore
) {

    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. 서버에 로그아웃 요청 및 토큰 삭제
            authRepository.logout()

            // 2. 사용자 정보 삭제
            preferencesDataStore.clearUserId()

            // 3. 온보딩 플래그는 유지

            Result.Success(Unit)
        } catch (e: Exception) {
            // 로그아웃은 어떤 경우에도 성공으로 처리
            Result.Success(Unit)
        }
    }
}
