package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.googleCloudBuild
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class GoogleCloudBuildProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.googleCloudBuild

            providers.shouldNotBeNull()
        }

        test("projectId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.projectId.shouldNotBeNull()
        }

        test("buildId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.buildId.shouldNotBeNull()
        }

        test("projectNumber provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.projectNumber.shouldNotBeNull()
        }

        test("location provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.location.shouldNotBeNull()
        }

        test("commitSha provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.commitSha.shouldNotBeNull()
        }

        test("shortSha provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.shortSha.shouldNotBeNull()
        }

        test("repoName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.repoName.shouldNotBeNull()
        }

        test("branchName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.branchName.shouldNotBeNull()
        }

        test("tagName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.tagName.shouldNotBeNull()
        }

        test("triggerName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.triggerName.shouldNotBeNull()
        }

        test("serviceAccountEmail provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.serviceAccountEmail.shouldNotBeNull()
        }

        test("prNumber is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            if (System.getenv("_PR_NUMBER") == null) {
                providers.prNumber.orNull.shouldBeNull()
            }
        }

        test("headBranch provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.headBranch.shouldNotBeNull()
        }

        test("baseBranch provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.googleCloudBuild

            providers.baseBranch.shouldNotBeNull()
        }
    }
}
