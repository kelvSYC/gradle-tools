package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A participant (author, reviewer, or observer) of a pull request in Bitbucket Data Center.
 */
@JsonClass(generateAdapter = false)
data class PullRequestParticipant(
    /**
     * The participant's user account.
     */
    val user: User? = null,

    /**
     * The participant's role: `AUTHOR`, `REVIEWER`, or `PARTICIPANT`.
     */
    val role: String? = null,

    /**
     * Whether the participant has approved the pull request.
     */
    val approved: Boolean? = null,

    /**
     * The participant's review status: `UNAPPROVED`, `APPROVED`, or `NEEDS_WORK`.
     */
    val status: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
