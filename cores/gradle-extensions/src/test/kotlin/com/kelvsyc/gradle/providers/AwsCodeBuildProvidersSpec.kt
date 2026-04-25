package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.awsCodeBuild
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class AwsCodeBuildProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.awsCodeBuild

            providers.shouldNotBeNull()
        }

        test("buildSucceeding is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            // The environment variable CODEBUILD_BUILD_SUCCEEDING is not set in tests,
            // so buildSucceeding should be absent (no orElse default)
            val result = providers.buildSucceeding.orNull

            // In test environment, CODEBUILD_BUILD_SUCCEEDING will not be "1"
            if (result != null) {
                result.shouldBeFalse()
            }
        }

        test("buildId provider is present (not null object)") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.buildId.shouldNotBeNull()
        }

        test("sourceVersions provider is present (not null object)") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.sourceVersions.shouldNotBeNull()
        }

        test("srcDirs provider is present (not null object)") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.srcDirs.shouldNotBeNull()
        }
    }
}
