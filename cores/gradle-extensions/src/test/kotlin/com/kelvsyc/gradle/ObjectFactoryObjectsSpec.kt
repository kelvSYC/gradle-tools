package com.kelvsyc.gradle

import com.kelvsyc.gradle.providers.AwsCodeBuildProviders
import com.kelvsyc.gradle.providers.AzurePipelinesProviders
import com.kelvsyc.gradle.providers.CircleCIProviders
import com.kelvsyc.gradle.providers.GitHubActionsProviders
import com.kelvsyc.gradle.providers.GitHubCodeBuildActionsProviders
import com.kelvsyc.gradle.providers.GitLabCIMergeRequestProviders
import com.kelvsyc.gradle.providers.GitLabCIProviders
import com.kelvsyc.gradle.providers.GoogleCloudBuildProviders
import com.kelvsyc.gradle.providers.TeamCityProviders
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

        test("azurePipelines returns AzurePipelinesProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.azurePipelines

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<AzurePipelinesProviders>()
        }

        test("azurePipelines is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.azurePipelines
            val second = project.objects.azurePipelines

            first.shouldBeInstanceOf<AzurePipelinesProviders>()
            second.shouldBeInstanceOf<AzurePipelinesProviders>()
        }

        test("circleCI returns CircleCIProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.circleCI

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<CircleCIProviders>()
        }

        test("circleCI is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.circleCI
            val second = project.objects.circleCI

            first.shouldBeInstanceOf<CircleCIProviders>()
            second.shouldBeInstanceOf<CircleCIProviders>()
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

        test("gitlabCI returns GitLabCIProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.gitlabCI

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<GitLabCIProviders>()
        }

        test("gitlabCI is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.gitlabCI
            val second = project.objects.gitlabCI

            first.shouldBeInstanceOf<GitLabCIProviders>()
            second.shouldBeInstanceOf<GitLabCIProviders>()
        }

        test("gitlabMergeRequest returns GitLabCIMergeRequestProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.gitlabMergeRequest

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<GitLabCIMergeRequestProviders>()
        }

        test("gitlabMergeRequest is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.gitlabMergeRequest
            val second = project.objects.gitlabMergeRequest

            first.shouldBeInstanceOf<GitLabCIMergeRequestProviders>()
            second.shouldBeInstanceOf<GitLabCIMergeRequestProviders>()
        }

        test("googleCloudBuild returns GoogleCloudBuildProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.googleCloudBuild

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<GoogleCloudBuildProviders>()
        }

        test("googleCloudBuild is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.googleCloudBuild
            val second = project.objects.googleCloudBuild

            first.shouldBeInstanceOf<GoogleCloudBuildProviders>()
            second.shouldBeInstanceOf<GoogleCloudBuildProviders>()
        }

        test("teamCity returns TeamCityProviders instance") {
            val project = ProjectBuilder.builder().build()

            val result = project.objects.teamCity

            result.shouldNotBeNull()
            result.shouldBeInstanceOf<TeamCityProviders>()
        }

        test("teamCity is cached across calls") {
            val project = ProjectBuilder.builder().build()

            val first = project.objects.teamCity
            val second = project.objects.teamCity

            first.shouldBeInstanceOf<TeamCityProviders>()
            second.shouldBeInstanceOf<TeamCityProviders>()
        }
    }
}
