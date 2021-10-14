package net.graphmasters.nunav.thesis.accuracy.notifier

import android.os.Handler
import net.graphmasters.multiplatform.core.location.Location
import net.graphmasters.multiplatform.core.units.Length
import net.graphmasters.multiplatform.navigation.location.LocationRepository
import net.graphmasters.multiplatform.navigation.routing.state.NavigationStateProvider
import net.graphmasters.nunav.android.base.app.BaseApplication
import net.graphmasters.nunav.location.gps.GpsFixProvider

class SchedulingGpsAccuracyNotifier(
    private val handler: Handler,
    private val gpsFixProvider: GpsFixProvider,
    private val navigationStateProvider: NavigationStateProvider
) : GpsAccuracyNotifier,
    LocationRepository.LocationUpdateListener {

    companion object {
        private const val MAX_ACCURACY_METERS = 50.0
        private const val MIN_TIME_ELAPSED_STARTUP_MS = 5000L
        private const val MIN_TIME_ELAPSED_RUNTIME_MS = 10000L
    }

    private val scheduledRunnable: Runnable = Runnable {
        this.validateAccuracy(this.accuracy)
        this.scheduleValidate(MIN_TIME_ELAPSED_RUNTIME_MS)
    }

    private val gpsAccuracyListeners = mutableSetOf<GpsAccuracyNotifier.GpsAccuracyListener>()

    private var inaccurateSince: Long? = System.currentTimeMillis()

    private var lastGpsUpdate = System.currentTimeMillis()

    private var accuracy: Length? = Length.fromMeters(100.0)

    private val shouldCallListeners: Boolean
        get() = navigationStateProvider.navigationState.currentlyNavigating
                && BaseApplication.getInstance().currentActivity.active

    override var gpsAccurate = false
        private set(value) {
            val previousValue = field
            field = value

            if (shouldCallListeners) {
                this.gpsAccuracyListeners.forEach {
                    when {
                        !previousValue && value -> it.onGpsAccurate()
                        previousValue && !value -> it.onGpsInaccurate()
                    }
                }
            }
        }

    init {
        this.scheduleValidate(MIN_TIME_ELAPSED_STARTUP_MS)
    }

    private fun scheduleValidate(delay: Long = 0) {
        this.handler.removeCallbacks(this.scheduledRunnable)
        this.handler.postDelayed(
            this.scheduledRunnable, delay
        )
    }

    override fun onLocationUpdated(location: Location) {
        this.accuracy = location.accuracy
        this.lastGpsUpdate = System.currentTimeMillis()

        this.scheduleValidate()
    }

    private fun validateAccuracy(accuracy: Length?) {
        when {
            (this.isPoorAccuracy(accuracy) || (this.hasNoGpsUpdateSinceTooLong())) -> {
                this.inaccurateSince.let {
                    if (this.isTrackingPoorAccuracy(it)) {
                        val inaccurateForTooLong =
                            System.currentTimeMillis() - it!! > MIN_TIME_ELAPSED_RUNTIME_MS

                        if (inaccurateForTooLong) {
                            this.gpsAccurate = false
                            this.inaccurateSince = null
                        }
                    } else {
                        this.inaccurateSince = System.currentTimeMillis()
                    }
                }
            }
            this.isGoodAccuracy(accuracy) -> {
                this.gpsAccurate = true
                this.inaccurateSince = null
            }
        }
    }

    private fun isTrackingPoorAccuracy(it: Long?) = it != null

    private fun isPoorAccuracy(accuracy: Length?) =
        accuracy == null || this.gpsAccurate && accuracy > Length.fromMeters(MAX_ACCURACY_METERS)

    private fun hasNoGpsUpdateSinceTooLong(): Boolean =
        System.currentTimeMillis() - this.lastGpsUpdate > MIN_TIME_ELAPSED_RUNTIME_MS &&
                System.currentTimeMillis() - this.gpsFixProvider.lastGpsFix > MIN_TIME_ELAPSED_RUNTIME_MS

    private fun isGoodAccuracy(accuracy: Length?) =
        accuracy != null && accuracy < Length.fromMeters(MAX_ACCURACY_METERS)

    override fun addGpsAccuracyListener(gpsAccuracyListener: GpsAccuracyNotifier.GpsAccuracyListener) {
        this.gpsAccuracyListeners.add(gpsAccuracyListener)
    }

    override fun removeGpsAccuracyListener(gpsAccuracyListener: GpsAccuracyNotifier.GpsAccuracyListener) {
        this.gpsAccuracyListeners.remove(gpsAccuracyListener)
    }
}