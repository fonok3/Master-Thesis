package net.graphmasters.nunav.thesis.usercount.repository

interface UserCountRepository {
    fun addOnUserCountChangedListener(userCountChangedListener: OnUserCountChangedListener)

    fun removeOnUserCountChangedListener(userCountChangedListener: OnUserCountChangedListener)

    val userCount: Int?

    interface OnUserCountChangedListener {
        fun onUserCountChanged(userCount: Int?)
    }
}