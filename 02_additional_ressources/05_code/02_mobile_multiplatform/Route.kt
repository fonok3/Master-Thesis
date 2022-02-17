...

data class Route(

    ...

    val trafficVolume: TrafficVolume,
) {

    ...

    enum class TrafficVolume {
        LITTLE, NORMAL, ELEVATED, HIGH
    }
}
