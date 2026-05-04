package com.kelvsyc.gradle.internal.aws.java.lambda

import com.kelvsyc.gradle.aws.java.lambda.MockLambdaClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.lambda.LambdaClient

abstract class MockLambdaClientInfoInternal : MockLambdaClientInfo, ServiceClientInfoInternal<LambdaClient> {
    override fun createClient(): LambdaClient = mockk()
}
