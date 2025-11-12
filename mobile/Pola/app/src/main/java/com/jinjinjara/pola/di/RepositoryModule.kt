package com.jinjinjara.pola.di

import com.jinjinjara.pola.data.repository.AuthRepositoryImpl
import com.jinjinjara.pola.data.repository.FileUploadRepositoryImpl
import com.jinjinjara.pola.data.repository.HomeRepositoryImpl
import com.jinjinjara.pola.data.repository.CategoryRepositoryImpl
import com.jinjinjara.pola.data.repository.ContentRepositoryImpl
import com.jinjinjara.pola.data.repository.RemindRepositoryImpl
import com.jinjinjara.pola.data.repository.FavoriteRepositoryImpl
import com.jinjinjara.pola.data.repository.TimelineRepositoryImpl
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.domain.repository.ContentRepository
import com.jinjinjara.pola.domain.repository.FileUploadRepository
import com.jinjinjara.pola.domain.repository.HomeRepository
import com.jinjinjara.pola.domain.repository.RemindRepository
import com.jinjinjara.pola.domain.repository.FavoriteRepository
import com.jinjinjara.pola.domain.repository.TimelineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 구현체를 인터페이스에 바인딩하는 모듈
 *
 * @Binds를 사용하여 인터페이스와 구현체를 연결
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * AuthRepository 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * CategoryRepository 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    /**
     * UserRepository 바인딩
     */
//    @Binds
//    @Singleton
//    abstract fun bindUserRepository(
//        userRepositoryImpl: UserRepositoryImpl
//    ): UserRepository

    // 추가 Repository 바인딩 예시
    // @Binds
    // @Singleton
    // abstract fun bindPostRepository(
    //     postRepositoryImpl: PostRepositoryImpl
    // ): PostRepository

    @Binds
    @Singleton
    abstract fun bindFileUploadRepository(
        fileUploadRepositoryImpl: FileUploadRepositoryImpl
    ): FileUploadRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindRemindRepository(
        remindRepositoryImpl: RemindRepositoryImpl
    ): RemindRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(
        timelineRepositoryImpl: TimelineRepositoryImpl
    ): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository
}