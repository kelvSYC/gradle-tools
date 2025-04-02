package com.kelvsyc.gradle.internal.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import com.kelvsyc.gradle.aws.kotlin.ses.SesClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SesClientInfoInternal : SesClientInfo, ServiceClientInfoInternal<SesClient> {
    override fun createClient(): SesClient {
        return SesClient {
            if (this@SesClientInfoInternal.region.isPresent) {
                region = this@SesClientInfoInternal.region.get()
            }

            if (this@SesClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@SesClientInfoInternal.credentials.get()
            }
        }
    }
}
