// data/repository/ReportRepositoryImpl.kt
package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.data.remote.api.ReportApi
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.Report
import com.jinjinjara.pola.domain.repository.ReportRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ReportRepository implementation
 */
class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReportRepository {

    override suspend fun getMyReports(): Result<List<Report>> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Report:Repo", "Fetching user reports")
                val response = reportApi.getMyReports()

                Log.d("Report:Repo", "Response code: ${response.code()}")
                Log.d("Report:Repo", "Response message: ${response.message()}")
                Log.d("Report:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("Report:Repo", "Response body: $body")
                    Log.d("Report:Repo", "Response status: ${body.status}")

                    if (body.status == "SUCCESS") {
                        val reports = body.data.map { it.toDomain() }
                        Log.d("Report:Repo", "Successfully fetched ${reports.size} reports")
                        Result.Success(reports)
                    } else {
                        Log.e("Report:Repo", "API status is not SUCCESS")
                        Log.e("Report:Repo", "Status: ${body.status}")
                        Log.e("Report:Repo", "Message: ${body.message}")
                        Result.Error(
                            message = body.message,
                            errorType = ErrorType.SERVER
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Report:Repo", "Failed to fetch reports")
                    Log.e("Report:Repo", "Response code: ${response.code()}")
                    Log.e("Report:Repo", "Response message: ${response.message()}")
                    Log.e("Report:Repo", "Error body: $errorBody")

                    Result.Error(
                        message = "리포트 정보를 가져올 수 없습니다. (${response.code()})",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("Report:Repo", "Exception while fetching reports", e)
                Log.e("Report:Repo", "Exception type: ${e.javaClass.simpleName}")
                Log.e("Report:Repo", "Exception message: ${e.message}")
                e.printStackTrace()

                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다.",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }
}