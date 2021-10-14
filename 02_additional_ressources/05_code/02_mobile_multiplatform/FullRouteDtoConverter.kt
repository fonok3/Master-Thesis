...

    private fun convertTrafficVolume(etaQuality: String?): Route.TrafficVolume {
        return when (etaQuality) {
            "little" -> Route.TrafficVolume.LITTLE
            "normal" -> Route.TrafficVolume.NORMAL
            "elevated" -> Route.TrafficVolume.ELEVATED
            "high" -> Route.TrafficVolume.HIGH
            else -> Route.TrafficVolume.NORMAL
        }
    }

...
