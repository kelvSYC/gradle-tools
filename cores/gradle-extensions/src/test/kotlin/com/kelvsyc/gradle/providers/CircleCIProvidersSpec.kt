package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.circleCI
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class CircleCIProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.circleCI

            providers.shouldNotBeNull()
        }

        test("ci defaults to false when CI environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CI") == null) {
                providers.ci.get().shouldBeFalse()
            }
        }

        test("circleCI defaults to false when CIRCLECI environment variable is absent") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLECI") == null) {
                providers.circleCI.get().shouldBeFalse()
            }
        }

        test("buildNum is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLE_BUILD_NUM") == null) {
                providers.buildNum.orNull.shouldBeNull()
            }
        }

        test("nodeIndex is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLE_NODE_INDEX") == null) {
                providers.nodeIndex.orNull.shouldBeNull()
            }
        }

        test("nodeTotal is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLE_NODE_TOTAL") == null) {
                providers.nodeTotal.orNull.shouldBeNull()
            }
        }

        test("prNumber is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLE_PR_NUMBER") == null) {
                providers.prNumber.orNull.shouldBeNull()
            }
        }

        test("previousBuildNum is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            if (System.getenv("CIRCLE_PREVIOUS_BUILD_NUM") == null) {
                providers.previousBuildNum.orNull.shouldBeNull()
            }
        }

        test("sha1 provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.sha1.shouldNotBeNull()
        }

        test("branch provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.branch.shouldNotBeNull()
        }

        test("buildUrl provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.buildUrl.shouldNotBeNull()
        }

        test("workflowId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.workflowId.shouldNotBeNull()
        }

        test("workingDirectory provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.workingDirectory.shouldNotBeNull()
        }

        test("repositoryUrl provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.repositoryUrl.shouldNotBeNull()
        }

        test("pipelineId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.pipelineId.shouldNotBeNull()
        }

        test("organizationId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.circleCI

            providers.organizationId.shouldNotBeNull()
        }
    }
}
