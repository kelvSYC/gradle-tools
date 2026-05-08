package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.gitlabCI
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class GitLabCIProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.gitlabCI

            providers.shouldNotBeNull()
        }

        test("ci defaults to false when CI environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI") == null) {
                providers.ci.get().shouldBeFalse()
            }
        }

        test("gitlabCI defaults to false when GITLAB_CI environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("GITLAB_CI") == null) {
                providers.gitlabCI.get().shouldBeFalse()
            }
        }

        test("commitRefProtected is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_COMMIT_REF_PROTECTED") == null) {
                providers.commitRefProtected.orNull.shouldBeNull()
            }
        }

        test("debugTrace is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_DEBUG_TRACE") == null) {
                providers.debugTrace.orNull.shouldBeNull()
            }
        }

        test("serverPort is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_SERVER_PORT") == null) {
                providers.serverPort.orNull.shouldBeNull()
            }
        }

        test("serverVersionMajor is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_SERVER_VERSION_MAJOR") == null) {
                providers.serverVersionMajor.orNull.shouldBeNull()
            }
        }

        test("pipelineId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_PIPELINE_ID") == null) {
                providers.pipelineId.orNull.shouldBeNull()
            }
        }

        test("jobId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_JOB_ID") == null) {
                providers.jobId.orNull.shouldBeNull()
            }
        }

        test("jobTimeout is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_JOB_TIMEOUT") == null) {
                providers.jobTimeout.orNull.shouldBeNull()
            }
        }

        test("nodeIndex is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_NODE_INDEX") == null) {
                providers.nodeIndex.orNull.shouldBeNull()
            }
        }

        test("runnerId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_RUNNER_ID") == null) {
                providers.runnerId.orNull.shouldBeNull()
            }
        }

        test("userId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("GITLAB_USER_ID") == null) {
                providers.userId.orNull.shouldBeNull()
            }
        }

        test("environmentId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            if (System.getenv("CI_ENVIRONMENT_ID") == null) {
                providers.environmentId.orNull.shouldBeNull()
            }
        }

        test("projectDir provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            providers.projectDir.shouldNotBeNull()
        }

        test("commitSha provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            providers.commitSha.shouldNotBeNull()
        }

        test("projectUrl provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            providers.projectUrl.shouldNotBeNull()
        }

        test("registry provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabCI

            providers.registry.shouldNotBeNull()
        }
    }
}
