package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.imds.ImdsAsyncClientInfo
import com.kelvsyc.gradle.aws.java.imds.ImdsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class ImdsJavaBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info type") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(ImdsJavaBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain ImdsClientInfo::class.java
            clients.createableTypes shouldContain ImdsAsyncClientInfo::class.java
        }
    }
}
