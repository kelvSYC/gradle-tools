package com.kelvsyc.gradle.internal.aws.java.imds

import com.kelvsyc.gradle.aws.java.imds.MockImdsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.imds.Ec2MetadataClient

abstract class MockImdsClientInfoInternal : MockImdsClientInfo, ServiceClientInfoInternal<Ec2MetadataClient> {
    override fun createClient(): Ec2MetadataClient = mockk()
}
