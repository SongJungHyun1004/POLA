package com.jinjinjara.pola.domain.usecase.auth

import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.domain.usecase.BaseUseCase
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

// 로그인 UseCase
// Google OAuth 로그인
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<LoginUseCase.Params, Result<User>> {

    override suspend fun invoke(params: Params): Result<User> {
        return authRepository.googleLoginWithOAuth(
            idToken = params.idToken,
            displayName = params.displayName
        )
    }

    // 로그인 파라미터
    data class Params(
        val idToken: String,
        val displayName: String
    )
}