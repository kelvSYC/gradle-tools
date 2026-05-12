package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.azure.keyvault.SecretAsyncClientInfo
import com.kelvsyc.gradle.azure.keyvault.SecretClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class AzureKeyVaultBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info types") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AzureKeyVaultBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain SecretClientInfo::class.java
            clients.createableTypes shouldContain SecretAsyncClientInfo::class.java
        }
    }
}
