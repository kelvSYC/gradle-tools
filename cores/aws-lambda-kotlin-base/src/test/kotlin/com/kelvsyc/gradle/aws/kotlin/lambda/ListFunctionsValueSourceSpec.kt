package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.FunctionConfiguration
import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.lambda.model.ListFunctionsResponse
import aws.sdk.kotlin.services.lambda.paginators.listFunctionsPaginated
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.lambda.MockLambdaClientInfoInternal
import com.kelvsyc.gradle.plugins.LambdaKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ListFunctionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of function names to ARNs") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(LambdaKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}
            val client = extension.getClient<LambdaClient, MockLambdaClientInfo>("mock").get()!!

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

            val provider = project.providers.of(ListFunctionsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("fn-one" to "arn:aws:lambda:us-east-1:123:function:fn-one")
            result shouldContain ("fn-two" to "arn:aws:lambda:us-east-1:123:function:fn-two")
        }
    }
}
