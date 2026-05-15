package com.kelvsyc.gradle.azure.identity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.services.BuildServiceSpec
import org.gradle.testfixtures.ProjectBuilder

class ManagedIdentityCredentialBuildServiceSpec : FunSpec() {
    init {
        test("getClient - system-assigned returns non-null credential") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class.java) { spec: BuildServiceSpec<ManagedIdentityCredentialBuildService.Params> ->
                    spec.parameters.systemAssigned()
                }

            service.get().getClient().shouldNotBeNull()
        }

        test("getClient - user-assigned by clientId returns non-null credential") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi-client", ManagedIdentityCredentialBuildService::class.java) { spec: BuildServiceSpec<ManagedIdentityCredentialBuildService.Params> ->
                    spec.parameters.userAssigned("00000000-0000-0000-0000-000000000000")
                }

            service.get().getClient().shouldNotBeNull()
        }
    }
}
