package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.google.cloud.artifact.ArtifactRegistryClientInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class GoogleCloudArtifactRegistryBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info type") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain ArtifactRegistryClientInfo::class.java
        }
    }
}
