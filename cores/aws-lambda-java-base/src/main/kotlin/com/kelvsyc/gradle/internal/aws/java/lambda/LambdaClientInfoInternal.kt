package com.kelvsyc.gradle.internal.aws.java.lambda

import com.kelvsyc.gradle.aws.java.lambda.LambdaClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.lambda.LambdaClient

abstract class LambdaClientInfoInternal : LambdaClientInfo, ServiceClientInfoInternal<LambdaClient> {
    override fun createClient(): LambdaClient {
        return LambdaClient.builder().apply {
            if (region.isPresent) {
                region(region.get())
            }
            if (credentials.isPresent) {
                credentialsProvider(credentials.get())
            } else {
                credentialsProvider(AnonymousCredentialsProvider.create())
            }
        }.build()
    }
}
