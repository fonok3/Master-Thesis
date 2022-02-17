package net.graphmasters.nunav.thesis.accuracy.notifier

interface GpsAccuracyNotifier {
    val gpsAccurate: Boolean

    fun addGpsAccuracyListener(gpsAccuracyListener: GpsAccuracyListener)

    fun removeGpsAccuracyListener(gpsAccuracyListener: GpsAccuracyListener)

    interface GpsAccuracyListener {
        fun onGpsAccurate()

        fun onGpsInaccurate()
    }
}