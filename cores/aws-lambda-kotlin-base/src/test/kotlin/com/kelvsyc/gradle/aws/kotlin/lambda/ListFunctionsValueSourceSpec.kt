package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.FunctionConfiguration
import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.lambda.model.ListFunctionsResponse
import aws.sdk.kotlin.services.lambda.paginators.listFunctionsPaginated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListFunctionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of function names to ARNs") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)

            mockkStatic("aws.sdk.kotlin.services.lambda.paginators.PaginatorsKt")
            every { client.listFunctionsPaginated(any<ListFunctionsRequest>()) } returns flowOf(
                ListFunctionsResponse {
                    functions = listOf(
                        FunctionConfiguration {
                            functionName = "fn-one"
                            functionArn = "arn:aws:lambda:us-east-1:123:function:fn-one"
                        },
                        FunctionConfiguration {
                            functionName = "fn-two"
                            functionArn = "arn:aws:lambda:us-east-1:123:function:fn-two"
                        },
                    )
                }
            )

            val provider = project.providers.ofKt(ListFunctionsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("fn-one" to "arn:aws:lambda:us-east-1:123:function:fn-one")
            result shouldContain ("fn-two" to "arn:aws:lambda:us-east-1:123:function:fn-two")
        }
    }
}
