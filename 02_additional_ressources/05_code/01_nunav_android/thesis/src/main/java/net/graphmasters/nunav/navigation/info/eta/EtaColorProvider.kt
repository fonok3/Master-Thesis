package net.graphmasters.nunav.navigation.info.eta

import android.content.Context
import android.graphics.Color
import net.graphmasters.multiplatform.navigation.model.Route

interface EtaColorProvider {
    fun getColorForTrafficVolume(trafficVolume: Route.TrafficVolume, defaultColor: Int, context: Context): Int
}