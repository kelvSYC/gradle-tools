package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse
import software.amazon.awssdk.services.lambda.paginators.ListFunctionsIterable
import java.util.stream.Stream

class ListFunctionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of function names to ARNs") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)

            val fn1 = mockk<FunctionConfiguration>()
            every { fn1.functionName() } returns "fn-one"
            every { fn1.functionArn() } returns "arn:aws:lambda:us-east-1:123:function:fn-one"

            val fn2 = mockk<FunctionConfiguration>()
            every { fn2.functionName() } returns "fn-two"
            every { fn2.functionArn() } returns "arn:aws:lambda:us-east-1:123:function:fn-two"

            val response = mockk<ListFunctionsResponse>()
            every { response.functions() } returns listOf(fn1, fn2)

            val paginator = mockk<ListFunctionsIterable>()
            every { paginator.stream() } returns Stream.of(response)

            every { client.listFunctionsPaginator(any<ListFunctionsRequest>()) } returns paginator

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
