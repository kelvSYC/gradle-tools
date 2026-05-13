package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient

/**
 * Test-only [TopicAdminClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockTopicAdminClientBuildService : TopicAdminClientBuildService() {
    override fun createClient(): TopicAdminClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: TopicAdminClient? = null
    }
}
