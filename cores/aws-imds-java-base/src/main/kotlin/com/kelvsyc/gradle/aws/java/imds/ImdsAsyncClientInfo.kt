package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property
import software.amazon.awssdk.imds.Ec2MetadataAsyncClient
import software.amazon.awssdk.imds.EndpointMode

interface ImdsAsyncClientInfo : ServiceClientInfo<Ec2MetadataAsyncClient> {
    val endpoint: Property<String>
    val endpointMode: Property<EndpointMode>
}
