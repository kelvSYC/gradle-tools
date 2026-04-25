package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.githubCodeBuildActions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class GitHubCodeBuildActionsProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.githubCodeBuildActions

            providers.shouldNotBeNull()
        }

        test("runnerWithBuildspec defaults to false when environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubCodeBuildActions

            if (System.getenv("CODEBUILD_RUNNER_WITH_BUILDSPEC") == null) {
                providers.runnerWithBuildspec.get().shouldBeFalse()
            }
        }

        test("runnerOwner provider is present (not null object)") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubCodeBuildActions

            providers.runnerOwner.shouldNotBeNull()
        }

        test("webhookLabel provider is present (not null object)") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.githubCodeBuildActions

            providers.webhookLabel.shouldNotBeNull()
        }
    }
}
