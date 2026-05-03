package com.kelvsyc.gradle.internal.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import com.kelvsyc.gradle.aws.kotlin.lambda.LambdaClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class LambdaClientInfoInternal : LambdaClientInfo, ServiceClientInfoInternal<LambdaClient> {
    override fun createClient(): LambdaClient {
        return LambdaClient {
            if (this@LambdaClientInfoInternal.region.isPresent) {
                region = this@LambdaClientInfoInternal.region.get()
            }

            if (this@LambdaClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@LambdaClientInfoInternal.credentials.get()
            }
        }
    }
}
