package net.graphmasters.nunav.navigation.routing.route.provider.dto

import net.graphmasters.multiplatform.core.model.LatLng
import net.graphmasters.multiplatform.navigation.model.Routable
import net.graphmasters.multiplatform.navigation.model.Route
import net.graphmasters.multiplatform.navigation.routing.route.provider.RouteProvider
import net.graphmasters.multiplatform.navigation.routing.route.provider.dto.RouteDataDto
import net.graphmasters.multiplatform.navigation.routing.route.provider.dto.RouteDto
import net.graphmasters.multiplatform.navigation.routing.route.provider.dto.RouteDtoConverter
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository

class FeatureAwareRouteDtoConverter(private val featuresRepository: FeaturesRepository, private val routeDtoConverter: RouteDtoConverter): RouteDtoConverter {
    override fun convert(routeDataDto: RouteDataDto): RouteProvider.RouteData {
        return routeDtoConverter.convert(routeDataDto)
    }

    override fun convert(
        routeDto: RouteDto,
        previousRoute: Route?,
        origin: LatLng?,
        destination: Routable?
    ): Route {
        return routeDtoConverter.convert(routeDto, previousRoute, origin, destination).let {
            if (!featuresRepository.isFeatureEnabled(FeaturesRepository.Feature.TRAFFIC_VOLUME)) {
                it.copy(trafficVolume = Route.TrafficVolume.NORMAL)
            } else {
                it
            }
        }
    }
}