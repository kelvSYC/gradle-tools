package com.kelvsyc.gradle.internal.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.BitbucketServerService
import com.kelvsyc.gradle.bitbucket.server.MockBitbucketServerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockBitbucketServerClientInfoInternal :
    MockBitbucketServerClientInfo,
    ServiceClientInfoInternal<BitbucketServerService> {
    override fun createClient(): BitbucketServerService = mockk()
}
