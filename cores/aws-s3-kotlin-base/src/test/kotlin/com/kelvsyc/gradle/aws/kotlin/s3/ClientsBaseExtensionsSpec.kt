package com.kelvsyc.gradle.aws.kotlin.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.S3KotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ClientsBaseExtensionsSpec : FunSpec() {
    init {
        test("registerAwsS3KotlinClient - registers a client by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3KotlinClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(S3ClientInfo::class)
            registrations shouldHaveSize 1
            registrations.names shouldContain "my-client"
        }

        test("registerAwsS3KotlinClient - second call with same name is a no-op") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()

            extension.registerAwsS3KotlinClient("my-client") {}
            extension.registerAwsS3KotlinClient("my-client") {}

            val registrations = extension.service.get().registrationsWithType(S3ClientInfo::class)
            registrations shouldHaveSize 1
        }
    }
}
