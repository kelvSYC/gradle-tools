package com.kelvsyc.gradle

import com.kelvsyc.gradle.providers.AwsCodeBuildProviders
import com.kelvsyc.gradle.providers.GitHubActionsProviders
import com.kelvsyc.gradle.providers.GitHubCodeBuildActionsProviders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class ObjectFactoryObjectsSpec : FunSpec() {
    init {
        test("awsCodeBuild returns AwsCodeBuildProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.awsCodeBuild

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<AwsCodeBuildProviders>()
        }

        test("awsCodeBuild is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.awsCodeBuild
            val second = project.objects.awsCodeBuild

            first.shouldBeInstanceOf<AwsCodeBuildProviders>()
            second.shouldBeInstanceOf<AwsCodeBuildProviders>()
        }

        test("githubActions returns GitHubActionsProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.githubActions

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<GitHubActionsProviders>()
        }

        test("githubCodeBuildActions returns GitHubCodeBuildActionsProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.githubCodeBuildActions

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<GitHubCodeBuildActionsProviders>()
        }
    }
}
