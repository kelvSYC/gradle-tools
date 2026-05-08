package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.awsCodeBuild
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class AwsCodeBuildProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.awsCodeBuild

            providers.shouldNotBeNull()
        }

        test("buildSucceeding maps '1' to true") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            if (System.getenv("CODEBUILD_BUILD_SUCCEEDING") == "1") {
                providers.buildSucceeding.get().shouldBeTrue()
            }
        }

        test("buildSucceeding maps '0' to false") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            if (System.getenv("CODEBUILD_BUILD_SUCCEEDING") == "0") {
                providers.buildSucceeding.get().shouldBeFalse()
            }
        }

        test("buildSucceeding is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            if (System.getenv("CODEBUILD_BUILD_SUCCEEDING") == null) {
                providers.buildSucceeding.orNull.shouldBe(null)
            }
        }

        test("buildId provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.buildId.shouldNotBeNull()
        }

        test("buildUrl provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.buildUrl.shouldNotBeNull()
        }

        test("projectArn provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.projectArn.shouldNotBeNull()
        }

        test("sourceVersions provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.sourceVersions.shouldNotBeNull()
        }

        test("srcDir provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.srcDir.shouldNotBeNull()
        }

        test("srcDirs provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.awsCodeBuild

            providers.srcDirs.shouldNotBeNull()
        }
    }
}
