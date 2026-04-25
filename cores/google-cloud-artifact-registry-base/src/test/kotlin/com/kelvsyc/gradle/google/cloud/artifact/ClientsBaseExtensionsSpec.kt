package com.kelvsyc.gradle.google.cloud.artifact

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.GoogleCloudArtifactRegistryBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ClientsBaseExtensionsSpec : FunSpec() {
    init {
        test("registerGoogleCloudServiceClient - registers a client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerGoogleCloudServiceClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(ArtifactRegistryClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-client"
        }

        test("registerGoogleCloudServiceClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerGoogleCloudServiceClient("my-client") {}
            extension.registerGoogleCloudServiceClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(ArtifactRegistryClientInfo::class)
            registrations shouldHaveSize 1
        }
    }
}
