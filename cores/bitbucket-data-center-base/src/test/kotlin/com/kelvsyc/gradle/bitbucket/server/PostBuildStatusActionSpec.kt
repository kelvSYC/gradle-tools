package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.server.MockBitbucketServerClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketDataCenterBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class PostBuildStatusActionSpec : FunSpec() {
    init {
        test("execute - sends correct status fields") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<Void>>()
            every { call.execute() } returns Response.success(null)
            every { client.postBuildStatus("abc123", capture(bodySlot)) } returns call

            val params = project.objects.newInstance<PostBuildStatusAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.commitId.set("abc123")
            params.state.set("SUCCESSFUL")
            params.key.set("my-build")
            params.url.set("https://ci.example.com/builds/42")
            params.name.set("Build #42")
            params.description.set("All tests passed")

            val action = object : PostBuildStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body["state"] shouldBe "SUCCESSFUL"
            body["key"] shouldBe "my-build"
            body["url"] shouldBe "https://ci.example.com/builds/42"
            body["name"] shouldBe "Build #42"
            body["description"] shouldBe "All tests passed"
        }

        test("execute - omits optional fields when absent") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<Void>>()
            every { call.execute() } returns Response.success(null)
            every { client.postBuildStatus("abc123", capture(bodySlot)) } returns call

            val params = project.objects.newInstance<PostBuildStatusAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.commitId.set("abc123")
            params.state.set("INPROGRESS")
            params.key.set("my-build")
            params.url.set("https://ci.example.com/builds/42")

            val action = object : PostBuildStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body.containsKey("name") shouldBe false
            body.containsKey("description") shouldBe false
        }
    }
}
