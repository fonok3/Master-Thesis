package net.graphmasters.nunav.navigation.info.eta

import android.content.Context
import net.graphmasters.multiplatform.navigation.model.Route
import net.graphmasters.nunav.android.utils.ResourceUtils
import net.graphmasters.nunav.thesis.R

class TrafficVolumeAwareEtaColorProvider : EtaColorProvider {
    override fun getColorForTrafficVolume(
        trafficVolume: Route.TrafficVolume,
        defaultColor: Int,
        context: Context
    ): Int {
        return when (trafficVolume) {
            Route.TrafficVolume.LITTLE -> ResourceUtils.getColorByReference(
                context,
                R.attr.traffic_volume_little,
                defaultColor
            )
            Route.TrafficVolume.ELEVATED -> ResourceUtils.getColorByReference(
                context,
                R.attr.traffic_volume_elevated,
                defaultColor
            )
            Route.TrafficVolume.HIGH -> ResourceUtils.getColorByReference(
                context,
                R.attr.traffic_volume_high,
                defaultColor
            )
            else -> defaultColor
        }
    }
}