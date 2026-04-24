package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.S3JavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ClientsBaseExtensionsSpec : FunSpec() {
    init {
        test("registerAwsS3JavaClient - registers a synchronous client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3JavaClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(S3ClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-client"
        }

        test("registerAwsS3JavaClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3JavaClient("my-client") {}
            extension.registerAwsS3JavaClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(S3ClientInfo::class)
            registrations shouldHaveSize 1
        }

        test("registerAwsS3AsyncJavaClient - registers an asynchronous client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3AsyncJavaClient("my-async-client") {}

            val registrations = extension.service.get().registrationsWithType(S3AsyncClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-async-client"
        }

        test("registerAwsS3AsyncJavaClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3AsyncJavaClient("my-async-client") {}
            extension.registerAwsS3AsyncJavaClient("my-async-client") {}

            val registrations = extension.service.get().registrationsWithType(S3AsyncClientInfo::class)
            registrations shouldHaveSize 1
        }

        test("registerAwsS3TransferManagerJavaClient - registers a transfer manager client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3TransferManagerJavaClient("my-tm-client") {}

            val registrations = extension.service.get().registrationsWithType(S3TransferManagerClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-tm-client"
        }

        test("registerAwsS3TransferManagerJavaClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3TransferManagerJavaClient("my-tm-client") {}
            extension.registerAwsS3TransferManagerJavaClient("my-tm-client") {}

            val registrations = extension.service.get().registrationsWithType(S3TransferManagerClientInfo::class)
            registrations shouldHaveSize 1
        }
    }
}
