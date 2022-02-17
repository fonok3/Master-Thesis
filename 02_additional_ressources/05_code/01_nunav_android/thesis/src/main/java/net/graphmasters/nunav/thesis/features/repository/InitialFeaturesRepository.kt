package net.graphmasters.nunav.thesis.features.repository

import net.graphmasters.multiplatform.core.logging.GMLog
import net.graphmasters.nunav.thesis.features.provider.FeaturesProvider
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository.FeaturesLoadedListener
import java.util.concurrent.Executor

class InitialFeaturesRepository(
    private val executor: Executor,
    private val testableFeaturesProvider: FeaturesProvider
) :
    FeaturesRepository {

    override fun refresh() {
        this.executor.execute {
            this.availableFeatures = this.testableFeaturesProvider.features
        }
    }

    override fun isFeatureEnabled(id: String): Boolean {
        return availableFeatures[id] ?: false
    }

    override var availableFeatures: Map<String, Boolean> = mutableMapOf()
        set(value) {
            field = value
            this.listeners.forEach { listener ->
                listener.onFeaturesLoaded()
            }
        }

    private val listeners = mutableSetOf<FeaturesLoadedListener>()

    override fun addFeaturesLoadedListener(featuresLoadedListener: FeaturesLoadedListener) {
        this.listeners.add(featuresLoadedListener)
    }

    override fun removeFeaturesLoadedListener(featuresLoadedListener: FeaturesLoadedListener) {
        this.listeners.remove(featuresLoadedListener)
    }
}
