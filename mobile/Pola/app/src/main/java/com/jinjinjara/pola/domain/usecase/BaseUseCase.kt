package com.jinjinjara.pola.domain.usecase

/**
 * UseCase 기본 인터페이스
 * - 단일 책임 원칙을 따르는 비즈니스 로직 단위
 * - suspend function으로 비동기 작업 지원
 *
 * @param Params 입력 파라미터 타입
 * @param ReturnType 반환 타입
 */
interface BaseUseCase<in Params, out ReturnType> {
    /**
     * UseCase 실행
     */
    suspend operator fun invoke(params: Params): ReturnType
}

/**
 * 파라미터가 없는 UseCase
 */
interface NoParamsUseCase<out ReturnType> {
    suspend operator fun invoke(): ReturnType
}
