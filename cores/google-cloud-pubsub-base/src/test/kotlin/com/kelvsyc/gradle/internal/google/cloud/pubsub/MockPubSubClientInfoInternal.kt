package com.kelvsyc.gradle.internal.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.pubsub.MockPubSubClientInfo
import io.mockk.mockk

abstract class MockPubSubClientInfoInternal : MockPubSubClientInfo, ServiceClientInfoInternal<TopicAdminClient> {
    override fun createClient(): TopicAdminClient = mockk()
}
