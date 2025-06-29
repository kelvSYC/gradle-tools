package com.kelvsyc.gradle.clients

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ClientsBaseServiceSpec : FunSpec() {
    interface Dummy

    interface DummyInfo : ServiceClientInfo<Dummy>

    abstract class DummyInfoInternal : DummyInfo, ServiceClientInfoInternal<Dummy> {
        override fun createClient(): Dummy {
            return object : Dummy {}
        }
    }

    init {
        test("Init") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)

            service.get().registrations.shouldBeEmpty()
        }

        test("Register Binding - Java") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)

            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)

            // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
            val clients = service.get().registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain DummyInfo::class.java
            service.get().registrations.shouldBeEmpty()
            service.get().registrationsWithType(DummyInfo::class.java).shouldBeEmpty()
        }

        test("Register Binding - Kotlin") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)

            service.get().registerBinding(DummyInfo::class, DummyInfoInternal::class)

            // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
            val clients = service.get().registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain DummyInfo::class.java
            service.get().registrations.shouldBeEmpty()
            service.get().registrationsWithType(DummyInfo::class).shouldBeEmpty()
        }

        test("Register Client - Kotlin") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)

            service.get().registerIfAbsent<DummyInfo>("dummy") {}

            service.get().registrations.shouldHaveSize(1)
            service.get().registrations.names shouldContain "dummy"
            service.get().registrationsWithType(DummyInfo::class).shouldHaveSize(1)
            service.get().registrationsWithType(DummyInfo::class).names shouldContain "dummy"
        }

        test("Register Client - Java") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)

            service.get().registerIfAbsent("dummy", DummyInfo::class.java) {}

            service.get().registrations.shouldHaveSize(1)
            service.get().registrations.names shouldContain "dummy"
            service.get().registrationsWithType(DummyInfo::class).shouldHaveSize(1)
            service.get().registrationsWithType(DummyInfo::class).names shouldContain "dummy"
        }

        test("Get Client - Kotlin") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)
            service.get().registerIfAbsent<DummyInfo>("dummy") {}

            val actual1 = service.get().getClient<Dummy, _>("dummy")
            actual1.shouldNotBeNull()
            val actual2 = service.get().getClient<Dummy, _>("dummy")
            actual2 shouldBeSameInstanceAs actual1
        }

        test("Get Client - Java") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)
            service.get().registerIfAbsent("dummy", DummyInfo::class.java) {}

            val actual1 = service.get().getClient("dummy", DummyInfo::class.java, Dummy::class.java)
            actual1.shouldNotBeNull()
            val actual2 = service.get().getClient("dummy", DummyInfo::class.java, Dummy::class.java)
            actual2 shouldBeSameInstanceAs actual1
        }

        test("Get Client Null - Kotlin") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)

            val actual1 = service.get().getClient<Dummy, _>("dummy")
            actual1.shouldBeNull()
        }

        test("Get Client Null - Java") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("clients-base", ClientsBaseService::class)
            service.get().registerBinding(DummyInfo::class.java, DummyInfoInternal::class.java)

            val actual1 = service.get().getClient("dummy", DummyInfo::class.java, Dummy::class.java)
            actual1.shouldBeNull()
        }
    }
}
