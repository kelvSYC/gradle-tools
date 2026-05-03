package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ssm.MockSsmClientInfoInternal
import com.kelvsyc.gradle.plugins.SsmJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.paginators.GetParametersByPathIterable
import java.util.stream.Stream

class GetParametersByPathValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of parameter names to values") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SsmJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSsmClientInfo::class, MockSsmClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSsmClientInfo>("mock") {}
            val requestSlot = slot<GetParametersByPathRequest>()
            val client = extension.getClient<SsmClient, _>("mock").get()

            val param1 = mockk<Parameter>()
            every { param1.name() } returns "/app/one"
            every { param1.value() } returns "value-one"

            val param2 = mockk<Parameter>()
            every { param2.name() } returns "/app/two"
            every { param2.value() } returns "value-two"

            val response = mockk<GetParametersByPathResponse>()
            every { response.parameters() } returns listOf(param1, param2)

            val paginator = mockk<GetParametersByPathIterable>()
            every { paginator.stream() } returns Stream.of(response)

            every { client.getParametersByPathPaginator(capture(requestSlot)) } returns paginator

            val provider = project.providers.of(GetParametersByPathValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.path.set("/app/")
                parameters.recursive.set(true)
                parameters.withDecryption.set(true)
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("/app/one" to "value-one")
            result shouldContain ("/app/two" to "value-two")
            requestSlot.captured.path() shouldBe "/app/"
            requestSlot.captured.recursive() shouldBe true
            requestSlot.captured.withDecryption() shouldBe true
        }
    }
}
