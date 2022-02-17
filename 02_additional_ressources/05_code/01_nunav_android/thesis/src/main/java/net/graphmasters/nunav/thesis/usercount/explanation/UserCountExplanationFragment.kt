package net.graphmasters.nunav.thesis.usercount.explanation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import net.graphmasters.nunav.android.base.ui.fragment.NunavFragment
import net.graphmasters.nunav.core.logger.GMLog
import net.graphmasters.nunav.core.logger.infrastructure.LogEntryKey
import net.graphmasters.nunav.core.logger.infrastructure.Logger
import net.graphmasters.nunav.thesis.logging.ExplanationLog
import net.graphmasters.nunav.thesis.usercount.repository.UserCountRepository
import net.graphmasters.nunav.thesis.R

class UserCountExplanationFragment : NunavFragment(), View.OnClickListener {
    companion object {
        private const val TAG = "UserCountExplanationFragment"
    }

    var userCountRepository: UserCountRepository? = null
    private val creationTime = System.currentTimeMillis()
    private var longExplanationShown = false

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_count_information, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.explanation_text_view)?.text =
            userCountRepository?.userCount?.let {
                String.format(
                    getString(R.string.users_in_system_explanation),
                    userCountRepository?.userCount.toString()
                )
            }
                ?: getString(R.string.app_name) + " " + getString(R.string.users_in_system_explanation_fallback)

        view.findViewById<View>(R.id.learnMoreButton)?.setOnClickListener(this)
        view.findViewById<View>(R.id.cancelButton)?.setOnClickListener(this)
    }

    override fun onStop() {
        super.onStop()
        if (longExplanationShown) {
            GMLog.o(
                TAG, Logger.EntryBuilder.getNewInstance()
                    .add(
                        "Explanation", ExplanationLog(
                            ExplanationLog.ExplanationType.USER_COUNT_EXPLANATION_LONG,
                            System.currentTimeMillis() - creationTime
                        )
                    )
                    .add(LogEntryKey.Subject.INFO)
                    .build()
            )
        }
        GMLog.o(
            TAG, Logger.EntryBuilder.getNewInstance()
                .add(
                    "Explanation", ExplanationLog(
                        ExplanationLog.ExplanationType.USER_COUNT_EXPLANATION_SHORT,
                        System.currentTimeMillis() - creationTime
                    )
                )
                .add(LogEntryKey.Subject.INFO)
                .build()
        )
    }

    override fun onClick(view: View) {
        if (this.isDetached) {
            return
        }
        when (view.id) {
            R.id.learnMoreButton -> this.showExplanation()
        }
        this.dismiss()
    }

    private fun showExplanation() {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.route_explanation_collaborative_url)))
        this.startActivity(browserIntent)
        this.longExplanationShown = true
    }
}