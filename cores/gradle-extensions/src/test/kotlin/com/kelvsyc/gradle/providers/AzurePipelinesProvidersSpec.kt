package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.azurePipelines
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class AzurePipelinesProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.azurePipelines

            providers.shouldNotBeNull()
        }

        test("tfBuild defaults to false when TF_BUILD environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("TF_BUILD") == null) {
                providers.tfBuild.get().shouldBeFalse()
            }
        }

        test("debug is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("SYSTEM_DEBUG") == null) {
                providers.debug.orNull.shouldBeNull()
            }
        }

        test("buildId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("BUILD_BUILDID") == null) {
                providers.buildId.orNull.shouldBeNull()
            }
        }

        test("agentId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("AGENT_ID") == null) {
                providers.agentId.orNull.shouldBeNull()
            }
        }

        test("definitionVersion is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("BUILD_DEFINITIONVERSION") == null) {
                providers.definitionVersion.orNull.shouldBeNull()
            }
        }

        test("jobAttempt is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("SYSTEM_JOBATTEMPT") == null) {
                providers.jobAttempt.orNull.shouldBeNull()
            }
        }

        test("stageAttempt is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("SYSTEM_STAGEATTEMPT") == null) {
                providers.stageAttempt.orNull.shouldBeNull()
            }
        }

        test("pullRequestId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("SYSTEM_PULLREQUEST_PULLREQUESTID") == null) {
                providers.pullRequestId.orNull.shouldBeNull()
            }
        }

        test("pullRequestIsFork is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("SYSTEM_PULLREQUEST_ISFORK") == null) {
                providers.pullRequestIsFork.orNull.shouldBeNull()
            }
        }

        test("triggeredByBuildId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("BUILD_TRIGGEREDBY_BUILDID") == null) {
                providers.triggeredByBuildId.orNull.shouldBeNull()
            }
        }

        test("environmentId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            if (System.getenv("ENVIRONMENT_ID") == null) {
                providers.environmentId.orNull.shouldBeNull()
            }
        }

        test("sourceBranch provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.sourceBranch.shouldNotBeNull()
        }

        test("sourceVersion provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.sourceVersion.shouldNotBeNull()
        }

        test("buildReason provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.buildReason.shouldNotBeNull()
        }

        test("teamProject provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.teamProject.shouldNotBeNull()
        }

        test("repositoryName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.repositoryName.shouldNotBeNull()
        }

        test("sourcesDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.sourcesDirectory.shouldNotBeNull()
        }

        test("artifactStagingDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.artifactStagingDirectory.shouldNotBeNull()
        }

        test("testResultsDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.testResultsDirectory.shouldNotBeNull()
        }

        test("pipelineWorkspace provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.pipelineWorkspace.shouldNotBeNull()
        }

        test("collectionUri provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.azurePipelines

            providers.collectionUri.shouldNotBeNull()
        }
    }
}
