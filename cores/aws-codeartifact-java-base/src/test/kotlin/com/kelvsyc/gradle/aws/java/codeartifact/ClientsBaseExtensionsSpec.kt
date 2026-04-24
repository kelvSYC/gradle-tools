package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.CodeArtifactJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ClientsBaseExtensionsSpec : FunSpec() {
    init {
        test("registerAwsCodeArtifactJavaClient - registers a synchronous client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsCodeArtifactJavaClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(CodeArtifactClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-client"
        }

        test("registerAwsCodeArtifactJavaClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsCodeArtifactJavaClient("my-client") {}
            extension.registerAwsCodeArtifactJavaClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(CodeArtifactClientInfo::class)
            registrations shouldHaveSize 1
        }

        test("registerAwsCodeArtifactAsyncJavaClient - registers an asynchronous client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsCodeArtifactAsyncJavaClient("my-async-client") {}

            val registrations = extension.service.get().registrationsWithType(CodeArtifactAsyncClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-async-client"
        }

        test("registerAwsCodeArtifactAsyncJavaClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsCodeArtifactAsyncJavaClient("my-async-client") {}
            extension.registerAwsCodeArtifactAsyncJavaClient("my-async-client") {}

            val registrations = extension.service.get().registrationsWithType(CodeArtifactAsyncClientInfo::class)
            registrations shouldHaveSize 1
        }
    }
}
