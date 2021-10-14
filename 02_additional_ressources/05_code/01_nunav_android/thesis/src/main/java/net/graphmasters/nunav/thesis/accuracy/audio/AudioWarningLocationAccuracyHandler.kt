package net.graphmasters.nunav.thesis.accuracy.audio

import net.graphmasters.nunav.android.base.app.ContextProvider
import net.graphmasters.nunav.audio.player.AudioJob
import net.graphmasters.nunav.audio.player.AudioJobPlayer
import net.graphmasters.nunav.thesis.R
import net.graphmasters.nunav.thesis.accuracy.notifier.GpsAccuracyNotifier
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository

class AudioWarningLocationAccuracyHandler(
    private val contextProvider: ContextProvider,
    private val audioJobPlayer: AudioJobPlayer,
    private val featuresRepository: FeaturesRepository
) : GpsAccuracyNotifier.GpsAccuracyListener {

    private val audioNotificationActive: Boolean
        get() = featuresRepository.isFeatureEnabled(FeaturesRepository.Feature.GPS_QUALITY)

    override fun onGpsAccurate() {}

    override fun onGpsInaccurate() {
        if (this.audioNotificationActive) {
            this.audioJobPlayer.execute(
                AudioJob.Factory.createFromTextToSpeech(
                    this.contextProvider.activityContext.getString(
                        R.string.position_inaccurate
                    )
                )
            )
        }
    }
}