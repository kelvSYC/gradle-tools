package com.kelvsyc.gradle.internal.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import com.kelvsyc.gradle.aws.kotlin.ecr.EcrClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class EcrClientInfoInternal : EcrClientInfo, ServiceClientInfoInternal<EcrClient> {
    override fun createClient(): EcrClient {
        return EcrClient {
            if (this@EcrClientInfoInternal.region.isPresent) {
                region = this@EcrClientInfoInternal.region.get()
            }

            if (this@EcrClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@EcrClientInfoInternal.credentials.get()
            }
        }
    }
}
