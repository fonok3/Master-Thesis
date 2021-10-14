package net.graphmasters.nunav.thesis.accuracy.layer.handler

import net.graphmasters.nunav.android.base.app.ContextProvider
import net.graphmasters.nunav.android.utils.ResourceUtils
import net.graphmasters.nunav.map.layer.MapLayer
import net.graphmasters.nunav.mapbox.location.CurrentLocationMapLayer
import net.graphmasters.nunav.thesis.R
import net.graphmasters.nunav.thesis.accuracy.notifier.GpsAccuracyNotifier

class AccuracyMapLayerHandler(
    private val currentLocationLayer: MapLayer,
    private val contextProvider: ContextProvider
) : GpsAccuracyNotifier.GpsAccuracyListener {

    override fun onGpsAccurate() {
        (currentLocationLayer as? CurrentLocationMapLayer)?.setLocationAccurate()
    }

    override fun onGpsInaccurate() {
        (currentLocationLayer as? CurrentLocationMapLayer)?.setLocationInaccurate()
    }
}