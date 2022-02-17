package net.graphmasters.nunav.thesis.usercount.repository

import net.graphmasters.multiplatform.navigation.routing.events.NavigationEventHandler
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository
import net.graphmasters.nunav.thesis.usercount.provider.UserCountProvider
import java.util.concurrent.Executor

class RoutingAwareUserCountRepository(
    private val executor: Executor,
    private val userCountProvider: UserCountProvider,
    private val featuresRepository: FeaturesRepository
) : UserCountRepository, NavigationEventHandler.OnNavigationStoppedListener, FeaturesRepository.FeaturesLoadedListener {
    companion object {
        const val REFRESH_RATE_MS: Long = 10*60*1000
    }

    private var lastRefreshTimeStamp: Long = 0

    private var listeners =
        mutableSetOf<UserCountRepository.OnUserCountChangedListener>()

    override var userCount: Int? = null
        set(value) {
            field = value
            this.listeners.forEach { listener ->
                listener.onUserCountChanged(userCount)
            }
        }

    init {
        refreshUserCount()
    }

    override fun addOnUserCountChangedListener(userCountChangedListener: UserCountRepository.OnUserCountChangedListener) {
        this.listeners.add(userCountChangedListener)
    }

    override fun removeOnUserCountChangedListener(userCountChangedListener: UserCountRepository.OnUserCountChangedListener) {
        this.listeners.remove(userCountChangedListener)
    }

    override fun onNavigationStopped() {
        if ((System.currentTimeMillis() - this.lastRefreshTimeStamp) > REFRESH_RATE_MS) {
            this.refreshUserCount()
        }
    }

    private fun refreshUserCount() {
        if (this.featuresRepository.isFeatureEnabled(FeaturesRepository.Feature.USER_COUNT)) {
            this.executor.execute {
                this.lastRefreshTimeStamp = System.currentTimeMillis()
                this.userCount = userCountProvider.userCount
            }
        }
    }

    override fun onFeaturesLoaded() {
        if (userCount == null) {
            this.refreshUserCount()
        }
    }
}