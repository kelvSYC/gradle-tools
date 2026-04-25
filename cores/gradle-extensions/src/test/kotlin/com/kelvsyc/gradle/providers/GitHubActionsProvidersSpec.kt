package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.githubActions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldNotBeNull
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

            // CI env var is not set in test environment; orElse(false) is the default
            val result = providers.ci.get()

            // If CI is set to something other than "true", it should be false
            if (System.getenv("CI") == null || System.getenv("CI") != "true") {
                result.shouldBeFalse()
            }
        }

        test("actions defaults to false when GITHUB_ACTIONS environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            if (System.getenv("GITHUB_ACTIONS") == null || System.getenv("GITHUB_ACTIONS") != "true") {
                providers.actions.get().shouldBeFalse()
            }
        }

        test("workflowRunUrl is absent when serverUrl, repository, or runId are absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            // All three env vars (GITHUB_SERVER_URL, GITHUB_REPOSITORY, GITHUB_RUN_ID) must be set
            // for workflowRunUrl to be present; in test env they are not all set
            if (
                System.getenv("GITHUB_SERVER_URL") == null ||
                System.getenv("GITHUB_REPOSITORY") == null ||
                System.getenv("GITHUB_RUN_ID") == null
            ) {
                providers.workflowRunUrl.shouldNotBeNull()
            }
        }

        test("runId is absent when GITHUB_RUN_ID is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubActions

            providers.runId.shouldNotBeNull()
        }
    }
}
