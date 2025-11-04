package com.jinjinjara.pola.domain.usecase.auth

import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.domain.usecase.BaseUseCase
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

/**
 * 로그인 UseCase
 * - 이메일/비밀번호 로그인
 * - Google 로그인
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<LoginUseCase.Params, Result<User>> {

    override suspend fun invoke(params: Params): Result<User> {
        return when (params) {
            is Params.EmailPassword -> authRepository.login(
                email = params.email,
                password = params.password
            )
            is Params.Google -> authRepository.googleLoginWithOAuth(
                idToken = params.idToken,
                displayName = params.displayName
            )
        }
    }

    /**
     * 로그인 파라미터
     */
    sealed class Params {
        /**
         * 이메일/비밀번호 로그인
         */
        data class EmailPassword(
            val email: String,
            val password: String
        ) : Params()

        /**
         * Google 로그인
         */
        data class Google(
            val idToken: String,
            val displayName: String
        ) : Params()
    }
}