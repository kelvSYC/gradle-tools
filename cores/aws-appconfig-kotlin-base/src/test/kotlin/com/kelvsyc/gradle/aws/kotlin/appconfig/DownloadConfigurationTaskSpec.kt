package com.kelvsyc.gradle.aws.kotlin.appconfig

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DownloadConfigurationTaskSpec : FunSpec({
    test("executes and writes configuration bytes to output file") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigDataClientBuildService.fetchImpl = { _, _, _ ->
            "config content".toByteArray()
        }
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigDataClientBuildService::class)

        val outFile = project.layout.buildDirectory.file("config.txt").get().asFile
        outFile.parentFile.mkdirs()

        val task = project.tasks.create("t", DownloadConfigurationTask::class.java)
        task.service.set(service)
        task.applicationIdentifier.set("app-123")
        task.environmentIdentifier.set("env-456")
        task.configurationProfileIdentifier.set("profile-789")
        task.outputFile.set(outFile)

        task.execute()

        task.outputFile.get().asFile.readText() shouldBe "config content"
        MockAppConfigDataClientBuildService.fetchImpl = null
    }

    test("throws GradleException when fetchConfiguration returns null") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigDataClientBuildService.fetchImpl = { _, _, _ -> null }
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigDataClientBuildService::class)

        val outFile = project.layout.buildDirectory.file("config.txt").get().asFile
        outFile.parentFile.mkdirs()

        val task = project.tasks.create("t", DownloadConfigurationTask::class.java)
        task.service.set(service)
        task.applicationIdentifier.set("app-123")
        task.environmentIdentifier.set("env-456")
        task.configurationProfileIdentifier.set("profile-789")
        task.outputFile.set(outFile)

        val exception = shouldThrow<GradleException> { task.execute() }
        exception.message shouldBe
            "Failed to fetch configuration for app-123/env-456/profile-789"
        MockAppConfigDataClientBuildService.fetchImpl = null
    }
})
