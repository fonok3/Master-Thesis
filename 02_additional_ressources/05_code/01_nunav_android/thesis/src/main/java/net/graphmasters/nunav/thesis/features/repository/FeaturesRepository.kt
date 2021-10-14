package net.graphmasters.nunav.thesis.features.repository

interface FeaturesRepository {
    fun isFeatureEnabled(id: String): Boolean

    fun refresh()

    var availableFeatures: Map<String, Boolean>

    fun addFeaturesLoadedListener(enabledFeaturesChangedListener: FeaturesLoadedListener)

    fun removeFeaturesLoadedListener(enabledFeaturesChangedListener: FeaturesLoadedListener)

    interface FeaturesLoadedListener {
        fun onFeaturesLoaded()
    }

    interface Feature {
        companion object {
            const val TRAFFIC_VOLUME: String = "traffic-volume"
            const val GPS_QUALITY: String = "gps-quality"
            const val USER_COUNT: String = "user-count"
            const val ROUTE_EXPLANATION: String = "route-explanation"
        }
    }
}