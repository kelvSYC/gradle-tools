package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class BitbucketCloudBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info type") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(BitbucketCloudBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain BitbucketCloudClientInfo::class.java
        }
    }
}
