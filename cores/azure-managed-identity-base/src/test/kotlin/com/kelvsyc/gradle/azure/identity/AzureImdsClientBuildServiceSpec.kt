package com.kelvsyc.gradle.azure.identity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AzureImdsClientBuildServiceSpec : FunSpec() {
    init {
        test("getClient - returns non-null AzureImdsService") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", AzureImdsClientBuildService::class) {}

            service.get().getClient().shouldNotBeNull()
        }
    }
}
