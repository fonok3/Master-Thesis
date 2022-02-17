package net.graphmasters.nunav.thesis.usercount.provider

import retrofit2.Call
import retrofit2.http.GET
import net.graphmasters.multiplatform.core.logging.GMLog

class RetrofitUserCountProvider(
    private val retrofitProvider: RetrofitProvider
): UserCountProvider {
    private companion object {
        const val TAG = "RetrofitUserCountProvider"
    }

    override val userCount: Int?
        get() = retrieveUserCount()

    private fun retrieveUserCount(): Int? = try {
        retrofitProvider.getStatistics().execute()
            .body()?.body?.statistics?.currentlyActiveUsers
    } catch (e: Exception) {
        GMLog.e(TAG, e)
        null
    }

    interface RetrofitProvider {
        @GET("/v1/statistics/userCount")
        fun getStatistics(): Call<Body>

        data class Body(val body: ResponseDto)

        data class Statistics(val currentlyActiveUsers: Int?)

        data class ResponseDto(
            val statistics: Statistics?
        )
    }
}