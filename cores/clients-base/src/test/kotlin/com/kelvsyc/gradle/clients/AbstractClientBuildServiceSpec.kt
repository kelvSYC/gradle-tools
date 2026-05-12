package com.kelvsyc.gradle.clients

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractClientBuildServiceSpec : FunSpec() {
    interface Dummy

    abstract class DummyClientService : AbstractClientBuildService<Dummy, BuildServiceParameters.None>() {
        override fun createClient(): Dummy = object : Dummy {}
    }

    interface CloseableDummy : Dummy, AutoCloseable

    abstract class CloseableDummyClientService : AbstractClientBuildService<CloseableDummy, BuildServiceParameters.None>() {
        override fun createClient(): CloseableDummy = closeableClient!!
    }

    companion object {
        var closeableClient: CloseableDummy? = null
    }

    init {
        test("getClient returns a client") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("dummy", DummyClientService::class)

            val client = service.get().getClient()

            client.shouldNotBeNull()
        }

        test("getClient returns the same instance on repeated calls") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("dummy", DummyClientService::class)

            val client1 = service.get().getClient()
            val client2 = service.get().getClient()

            client2 shouldBeSameInstanceAs client1
        }

        test("close calls close on AutoCloseable clients") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("closeable", CloseableDummyClientService::class)
            var closed = false
            closeableClient = object : CloseableDummy {
                override fun close() {
                    closed = true
                }
            }

            service.get().getClient()
            service.get().close()

            closed.shouldBe(true)
        }

        test("close is a no-op when getClient was never called") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("closeable", CloseableDummyClientService::class)
            var closed = false
            closeableClient = object : CloseableDummy {
                override fun close() {
                    closed = true
                }
            }

            service.get().close()

            closed.shouldBe(false)
        }
    }
}
