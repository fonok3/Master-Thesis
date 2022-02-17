package net.graphmasters.nunav.thesis.features.provider

import net.graphmasters.multiplatform.core.logging.GMLog
import net.graphmasters.nunav.android.base.app.ContextProvider
import net.graphmasters.nunav.persistence.PreferenceManager
import net.graphmasters.nunav.thesis.R
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

class RetrofitFeaturesProvider(
    private val retrofitProvider: RetrofitProvider,
    private val contextProvider: ContextProvider
) : FeaturesProvider {
    private companion object {
        const val TAG = "RetrofitAvailableFeaturesProvider"
    }

    override val features: Map<String, Boolean>
        get() = try {
                this.retrieveTestableFeatures()
            } catch (e: Exception) {
                GMLog.e(TAG, e)
                emptyMap()
            }


    private fun retrieveTestableFeatures(): Map<String, Boolean> {
        val response = retrofitProvider.getAvailableFeatures(
            enableAllFeatures = PreferenceManager.getBoolean(R.string.settings_activate_all_server_features))
            .execute()

        return response.body()?.let { rawBody ->
            rawBody.body.features
        } ?: emptyMap()
    }

    interface RetrofitProvider {
        @GET("/v1/features/available")
        fun getAvailableFeatures(@Query("enableAllFeatures") enableAllFeatures: Boolean): Call<Body>

        data class Body(val body: ResponseDto)

        data class ResponseDto(
            val features: Map<String, Boolean>
        )
    }
}