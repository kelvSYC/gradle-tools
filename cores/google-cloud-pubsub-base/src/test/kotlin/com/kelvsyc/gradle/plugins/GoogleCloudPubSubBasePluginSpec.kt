package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.google.cloud.pubsub.PubSubClientInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class GoogleCloudPubSubBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info type") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(GoogleCloudPubSubBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain PubSubClientInfo::class.java
        }
    }
}
