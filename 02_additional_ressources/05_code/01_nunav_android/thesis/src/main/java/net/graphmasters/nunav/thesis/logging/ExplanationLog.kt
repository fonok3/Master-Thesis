package net.graphmasters.nunav.thesis.logging

import java.io.Serializable

data class ExplanationLog(val explanation: ExplanationType, val durationMs: Long? = null) {
    enum class ExplanationType {
        ROUTE_PREVIEW_EXPLANATION, USER_COUNT_EXPLANATION_SHORT, USER_COUNT_EXPLANATION_LONG
    }
}