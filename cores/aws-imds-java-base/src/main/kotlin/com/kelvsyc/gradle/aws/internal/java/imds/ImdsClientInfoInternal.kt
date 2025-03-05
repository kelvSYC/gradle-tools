package com.kelvsyc.gradle.aws.internal.java.imds

import com.kelvsyc.gradle.aws.java.imds.ImdsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.imds.Ec2MetadataClient
import java.net.URI

abstract class ImdsClientInfoInternal : ImdsClientInfo, ServiceClientInfoInternal<Ec2MetadataClient> {
    override fun createClient(): Ec2MetadataClient {
        return Ec2MetadataClient.builder().apply {
            if (endpoint.isPresent) endpoint(URI.create(endpoint.get()))
            if (endpointMode.isPresent) endpointMode(endpointMode.get())
        }.build()
    }
}
