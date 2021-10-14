...

func (routing *Routing) applyTrafficVolume(gmRoute *GMRoute) {
	if len(gmRoute.RouteLegs) > 0 {
		summedRealStepTime := 0.0
		summedNormalStepTime := 0.0

		for _, step := range gmRoute.RouteLegs[0].Steps {
			if step.AnticipatedSpeed != nil && step.LongTermAverageSpeed != nil {
				summedRealStepTime += (step.Length / *step.AnticipatedSpeed) + step.TurnInfo.TurnCost
				summedNormalStepTime += (step.Length / *step.LongTermAverageSpeed) + step.TurnInfo.TurnCost
			}
		}

		ratio := summedRealStepTime / summedNormalStepTime
		correctiveFactor := 1 - (math.Max(gmRoute.InitialLength, 10_000) / 10_000)

		if ratio < trafficVolumeRatioLittle - correctiveFactor {
			gmRoute.TrafficVolume = trafficVolumeLittle
		} else if ratio < trafficVolumeRatioNormal + correctiveFactor {
			gmRoute.TrafficVolume = trafficVolumeNormal
		} else if ratio < trafficVolumeRatioElevated + correctiveFactor{
			gmRoute.TrafficVolume = trafficVolumeElevated
		} else if ratio > 1 {
			gmRoute.TrafficVolume = trafficVolumeHigh
		}
	}
}

...