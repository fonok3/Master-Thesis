package net.graphmasters.nunav.thesis.accuracy.growl

import net.graphmasters.multiplatform.navigation.model.Routable
import net.graphmasters.multiplatform.navigation.routing.events.NavigationEventHandler
import net.graphmasters.nunav.android.base.app.ContextProvider
import net.graphmasters.nunav.android.base.infrastructure.growls.GrowlManager
import net.graphmasters.nunav.android.base.infrastructure.growls.GrowlManager.Growl
import net.graphmasters.nunav.android.utils.ResourceUtils
import net.graphmasters.nunav.android.utils.WindowUtils
import net.graphmasters.nunav.thesis.R
import net.graphmasters.nunav.thesis.accuracy.notifier.GpsAccuracyNotifier
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository

class LocationAccuracyGrowlHandler(
    private val growlManager: GrowlManager,
    private val featuresRepository: FeaturesRepository,
    private val accuracyNotifier: GpsAccuracyNotifier,
    contextProvider: ContextProvider
) : GpsAccuracyNotifier.GpsAccuracyListener, NavigationEventHandler.OnNavigationStartedListener,
    NavigationEventHandler.OnNavigationStoppedListener {

    private val growlEnabled: Boolean
        get() = featuresRepository.isFeatureEnabled(FeaturesRepository.Feature.GPS_QUALITY)

    private val growl: Growl by lazy {
        GrowlManager.GrowlBuilder.getNewInstance()
            .setTitle(contextProvider.applicationContext.getString(R.string.position_inaccurate))
            .setCloseable(false)
            .setIconPadding(WindowUtils.dpFromPx(contextProvider.applicationContext, 2F))
            .setIconColor(
                ResourceUtils.getColor(
                    contextProvider.applicationContext,
                    R.color.gps_inaccurate
                )
            )
            .setIconResource(R.drawable.ic_round_location_searching_24)
            .build()
    }

    private fun showGrowl() {
        if (!growlManager.containsGrowl(this.growl) && growlEnabled) {
            this.growlManager.addGrowl(this.growl)
        }
    }

    private fun hideGrowl() {
        this.growlManager.removeGrowl(this.growl)
    }

    override fun onGpsAccurate() {
        this.hideGrowl()
    }

    override fun onGpsInaccurate() {
        this.showGrowl()
    }

    override fun onNavigationStarted(routable: Routable) {
        if (!this.accuracyNotifier.gpsAccurate) {
            this.showGrowl()
        }
    }

    override fun onNavigationStopped() {
        this.hideGrowl()
    }
}
