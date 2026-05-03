package com.kelvsyc.gradle.internal.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import com.kelvsyc.gradle.aws.kotlin.ssm.SsmClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SsmClientInfoInternal : SsmClientInfo, ServiceClientInfoInternal<SsmClient> {
    override fun createClient(): SsmClient {
        return SsmClient {
            if (this@SsmClientInfoInternal.region.isPresent) {
                region = this@SsmClientInfoInternal.region.get()
            }

            if (this@SsmClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@SsmClientInfoInternal.credentials.get()
            }
        }
    }
}
