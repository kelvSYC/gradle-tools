package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.imds.EndpointMode

interface ImdsClientInfo : ServiceClientInfo<Ec2MetadataClient> {
    val endpoint: Property<String>
    val endpointMode: Property<EndpointMode>
}
