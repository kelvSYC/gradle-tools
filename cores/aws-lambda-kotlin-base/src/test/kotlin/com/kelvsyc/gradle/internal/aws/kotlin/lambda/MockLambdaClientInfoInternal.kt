package com.kelvsyc.gradle.internal.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import com.kelvsyc.gradle.aws.kotlin.lambda.MockLambdaClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockLambdaClientInfoInternal : MockLambdaClientInfo, ServiceClientInfoInternal<LambdaClient> {
    override fun createClient(): LambdaClient = mockk()
}
