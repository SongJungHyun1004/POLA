package com.jinjinjara.pola.di

import android.content.Context
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.data.remote.api.AuthApi
import com.jinjinjara.pola.data.remote.api.CategoryApi
import com.jinjinjara.pola.data.remote.api.ContentApi
import com.jinjinjara.pola.data.remote.api.FavoriteApi
import com.jinjinjara.pola.data.remote.api.FileUploadApi
import com.jinjinjara.pola.data.remote.api.HomeApi
import com.jinjinjara.pola.data.remote.api.RemindApi
import com.jinjinjara.pola.data.remote.api.TimelineApi
import com.jinjinjara.pola.data.remote.interceptor.AuthInterceptor
import com.jinjinjara.pola.data.remote.interceptor.TokenAuthenticator
import com.jinjinjara.pola.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// 네트워크 관련 의존성을 제공하는 모듈
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // Moshi 제공
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // HttpLoggingInterceptor 제공
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // AuthInterceptor 제공
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        preferencesDataStore: PreferencesDataStore
    ): AuthInterceptor {
        return AuthInterceptor(preferencesDataStore)
    }

    // TokenAuthenticator 제공
    // Lazy를 사용하여 순환 의존성 해결
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        preferencesDataStore: PreferencesDataStore,
        authApi: dagger.Lazy<AuthApi>,
        @ApplicationContext context: Context
    ): TokenAuthenticator {
        return TokenAuthenticator(preferencesDataStore, authApi, context)
    }

    // OkHttpClient 제공
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    // Retrofit 제공
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // AuthApi 제공
     @Provides
     @Singleton
     fun provideAuthApi(retrofit: Retrofit): AuthApi {
         return retrofit.create(AuthApi::class.java)
     }

    // CategoryApi 제공
    @Provides
    @Singleton
    fun provideCategoryApi(retrofit: Retrofit): CategoryApi {
        return retrofit.create(CategoryApi::class.java)
    }

    // 파일 업로드
    @Provides
    @Singleton
    fun provideFileUploadApi(retrofit: Retrofit): FileUploadApi {
        return retrofit.create(FileUploadApi::class.java)
    }

    // HomeApi
    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    // RemindApi
    @Provides
    @Singleton
    fun provideRemindApi(retrofit: Retrofit): RemindApi {
        return retrofit.create(RemindApi::class.java)
    }

    // FavoriteApi
    @Provides
    @Singleton
    fun provideFavoriteApi(retrofit: Retrofit): FavoriteApi {
        return retrofit.create(FavoriteApi::class.java)
    }

    // TimelineApi
    @Provides
    @Singleton
    fun provideTimelineApi(retrofit: Retrofit): TimelineApi {
        return retrofit.create(TimelineApi::class.java)
    }

    // ContentApi
    @Provides
    @Singleton
    fun proviceContentApi(retrofit: Retrofit): ContentApi {
        return retrofit.create(ContentApi::class.java)
    }
}