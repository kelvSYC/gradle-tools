package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.githubActions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class GitHubActionsProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.githubActions

            providers.shouldNotBeNull()
        }

        test("ci defaults to false when CI environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("CI") == null) {
                providers.ci.get().shouldBeFalse()
            }
        }

        test("actions defaults to false when GITHUB_ACTIONS environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_ACTIONS") == null) {
                providers.actions.get().shouldBeFalse()
            }
        }

        test("refProtected is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_REF_PROTECTED") == null) {
                providers.refProtected.orNull.shouldBeNull()
            }
        }

        test("retentionDays is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_RETENTION_DAYS") == null) {
                providers.retentionDays.orNull.shouldBeNull()
            }
        }

        test("runAttempt is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_RUN_ATTEMPT") == null) {
                providers.runAttempt.orNull.shouldBeNull()
            }
        }

        test("runId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_RUN_ID") == null) {
                providers.runId.orNull.shouldBeNull()
            }
        }

        test("runId maps to Long") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            val value = System.getenv("GITHUB_RUN_ID")
            if (value != null) {
                providers.runId.get().shouldBeInstanceOf<Long>()
                providers.runId.get().shouldBe(value.toLong())
            }
        }

        test("runNumber is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_RUN_NUMBER") == null) {
                providers.runNumber.orNull.shouldBeNull()
            }
        }

        test("runnerDebug is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("RUNNER_DEBUG") == null) {
                providers.runnerDebug.orNull.shouldBeNull()
            }
        }

        test("workflowRunUrl is absent when component variables are not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (
                System.getenv("GITHUB_SERVER_URL") == null ||
                System.getenv("GITHUB_REPOSITORY") == null ||
                System.getenv("GITHUB_RUN_ID") == null
            ) {
                providers.workflowRunUrl.orNull.shouldBeNull()
            }
        }

        test("envFile provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.envFile.shouldNotBeNull()
        }

        test("eventFile provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.eventFile.shouldNotBeNull()
        }

        test("outputFile provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.outputFile.shouldNotBeNull()
        }

        test("pathFile provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.pathFile.shouldNotBeNull()
        }

        test("stepSummaryFile provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.stepSummaryFile.shouldNotBeNull()
        }

        test("workspaceDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.workspaceDirectory.shouldNotBeNull()
        }

        test("runnerTempDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.runnerTempDirectory.shouldNotBeNull()
        }

        test("runnerToolCacheDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.runnerToolCacheDirectory.shouldNotBeNull()
        }
    }
}
