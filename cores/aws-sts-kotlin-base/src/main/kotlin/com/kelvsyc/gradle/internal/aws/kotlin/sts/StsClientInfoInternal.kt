package com.kelvsyc.gradle.internal.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import com.kelvsyc.gradle.aws.kotlin.sts.StsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class StsClientInfoInternal : StsClientInfo, ServiceClientInfoInternal<StsClient> {
    override fun createClient(): StsClient {
        return StsClient {
            if (this@StsClientInfoInternal.region.isPresent) {
                region = this@StsClientInfoInternal.region.get()
            }

            if (this@StsClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@StsClientInfoInternal.credentials.get()
            }
        }
    }
}
