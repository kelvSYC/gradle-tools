package com.kelvsyc.gradle.clients

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class ClientsBaseExtensionSpec : FunSpec() {
    interface Dummy

    interface DummyInfo : ServiceClientInfo<Dummy>

    abstract class DummyInfoInternal : DummyInfo, ServiceClientInfoInternal<Dummy> {
        override fun createClient(): Dummy {
            return object : Dummy {}
        }
    }

    init {
        test("getService") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            val extension = project.extensions.create<ClientsBaseExtension>("clients-base", service)

            extension.service shouldBeSameInstanceAs service
        }

        test("getClient - Kotlin") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)
            service.get().registerIfAbsent<DummyInfo>("dummy") {}
            val extension = project.extensions.create<ClientsBaseExtension>("clients-base", service)

            val actual1 = extension.getClient<Dummy, _>("dummy")
            val expected = service.get().getClient<Dummy, _>("dummy")
            actual1.get() shouldBeSameInstanceAs expected
        }

        test("getClient - Java") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)
            service.get().registerIfAbsent<DummyInfo>("dummy") {}
            val extension = project.extensions.create<ClientsBaseExtension>("clients-base", service)

            val actual1 = extension.getClient("dummy", DummyInfo::class.java, Dummy::class.java)
            val expected = service.get().getClient<Dummy, _>("dummy")
            actual1.get() shouldBeSameInstanceAs expected
        }
    }
}
