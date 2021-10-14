package net.graphmasters.nunav.thesis.usercount.growl

import androidx.fragment.app.Fragment
import net.graphmasters.multiplatform.navigation.model.Routable
import net.graphmasters.multiplatform.navigation.routing.events.NavigationEventHandler
import net.graphmasters.multiplatform.navigation.routing.state.NavigationStateProvider
import net.graphmasters.nunav.android.base.app.BaseApplication
import net.graphmasters.nunav.android.base.app.CurrentActivityProvider
import net.graphmasters.nunav.android.base.infrastructure.growls.GrowlManager
import net.graphmasters.nunav.android.base.ui.activity.FullscreenFragmentActivity
import net.graphmasters.nunav.android.base.ui.fragment.FullscreenFragment.AttachListener
import net.graphmasters.nunav.android.base.ui.fragment.FullscreenFragment.DetachListener
import net.graphmasters.nunav.android.utils.ViewUtils
import net.graphmasters.nunav.android.utils.WindowUtils
import net.graphmasters.nunav.thesis.R
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository
import net.graphmasters.nunav.thesis.usercount.explanation.UserCountExplanationFragment
import net.graphmasters.nunav.thesis.usercount.repository.UserCountRepository

class UserCountGrowlHandler(
    private val growlManager: GrowlManager,
    private val currentActivityProvider: CurrentActivityProvider,
    private val userCountRepository: UserCountRepository,
    private val featuresRepository: FeaturesRepository,
    private val navigationStateProvider: NavigationStateProvider
) : NavigationEventHandler.OnNavigationStartedListener,
    NavigationEventHandler.OnNavigationStoppedListener,
    UserCountRepository.OnUserCountChangedListener, FeaturesRepository.FeaturesLoadedListener {

    private var currentGrowl: GrowlManager.Growl? = null

    private fun getNewGrowl(): GrowlManager.Growl = GrowlManager.GrowlBuilder.getNewInstance()
        .setCloseable(false)
        .setIconResource(R.drawable.ic_user_count)
        .setTitle(this.userCountRepository.userCount?.toString())
        .setIconPadding(
            WindowUtils.pxFromDp(
                currentActivityProvider.currentActivity.applicationContext,
                4F
            )
        )
        .setOnClickRunnable { this.showExplanation() }
        .build()

    init {
        this.updateGrowl()
    }

    private val featureEnabled: Boolean
        get() = featuresRepository.isFeatureEnabled(FeaturesRepository.Feature.USER_COUNT)

    fun updateGrowl() {
        if (this.navigationStateProvider.navigationState.currentlyNavigating ||
            this.userCountRepository.userCount == null
        ) {
            this.hideGrowl()
        } else {
            this.showGrowl()
        }
    }

    private fun showGrowl() {
        if (this.featureEnabled) {
            hideGrowl()
            val newGrowl = getNewGrowl()
            this.growlManager.addGrowl(newGrowl)
            this.currentGrowl = newGrowl
        }
    }

    private fun hideGrowl() {
        this.currentGrowl?.let { this.growlManager.removeGrowl(it) }
        this.currentGrowl = null
    }

    override fun onNavigationStarted(routable: Routable) {
        this.updateGrowl()
    }

    override fun onNavigationStopped() {
        this.updateGrowl()
    }

    override fun onUserCountChanged(userCount: Int?) {
        this.updateGrowl()
    }

    private fun showExplanation() {
        this.currentActivityProvider.currentActivity?.let {
            val fragment = UserCountExplanationFragment()
            fragment.userCountRepository = this.userCountRepository
            fragment.show(
                currentActivityProvider.currentActivity.supportFragmentManager,
                BaseApplication.DIALOG_TAG
            )
        }
    }

    override fun onFeaturesLoaded() {
        this.updateGrowl()
    }
}