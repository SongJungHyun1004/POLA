package com.jinjinjara.pola.util

/**
 * 앱 전체에서 사용하는 상수
 */
object Constants {
    // API
    const val BASE_URL = "https://k13d204.p.ssafy.io/api/v1/"
    const val SHARE_URL = "https://k13d204.p.ssafy.io/sharedfile/"


    // Network Timeout
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Database
    const val DATABASE_NAME = "pola_database"
    const val DATABASE_VERSION = 1

    // DataStore
    const val DATASTORE_NAME = "pola_preferences"

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_PAGE = 1
}