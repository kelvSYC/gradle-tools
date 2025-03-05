package com.kelvsyc.gradle.aws.internal.java.imds

import com.kelvsyc.gradle.aws.java.imds.ImdsAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.imds.Ec2MetadataAsyncClient
import java.net.URI

abstract class ImdsAsyncClientInfoInternal : ImdsAsyncClientInfo, ServiceClientInfoInternal<Ec2MetadataAsyncClient> {
    override fun createClient(): Ec2MetadataAsyncClient {
        return Ec2MetadataAsyncClient.builder().apply {
            if (endpoint.isPresent) endpoint(URI.create(endpoint.get()))
            if (endpointMode.isPresent) endpointMode(endpointMode.get())
        }.build()
    }
}
