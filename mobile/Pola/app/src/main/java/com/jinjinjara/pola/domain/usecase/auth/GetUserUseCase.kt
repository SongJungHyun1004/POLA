package com.jinjinjara.pola.domain.usecase.auth

import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

/**
 * 사용자 정보 조회 UseCase
 */
class GetUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getUser()
    }
}
