package com.kelvsyc.gradle.internal.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudService
import com.kelvsyc.gradle.bitbucket.cloud.MockBitbucketCloudClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockBitbucketCloudClientInfoInternal :
    MockBitbucketCloudClientInfo,
    ServiceClientInfoInternal<BitbucketCloudService> {
    override fun createClient(): BitbucketCloudService = mockk()
}
