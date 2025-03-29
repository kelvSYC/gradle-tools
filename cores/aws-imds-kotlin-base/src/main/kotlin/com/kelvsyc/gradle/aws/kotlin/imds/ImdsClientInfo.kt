package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface ImdsClientInfo : ServiceClientInfo<ImdsClient> {
    val endpoint: Property<String>
}
