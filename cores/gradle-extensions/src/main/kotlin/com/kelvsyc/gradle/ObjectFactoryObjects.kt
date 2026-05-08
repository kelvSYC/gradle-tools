package com.kelvsyc.gradle

import com.kelvsyc.gradle.internal.AwsCodeBuildProvidersDelegate
import com.kelvsyc.gradle.internal.AzurePipelinesProvidersDelegate
import com.kelvsyc.gradle.internal.CircleCIProvidersDelegate
import com.kelvsyc.gradle.internal.GitHubActionsProvidersDelegate
import com.kelvsyc.gradle.internal.GitHubCodeBuildActionsProvidersDelegate
import com.kelvsyc.gradle.internal.GitLabCIMergeRequestProvidersDelegate
import com.kelvsyc.gradle.internal.GitLabCIProvidersDelegate
import com.kelvsyc.gradle.internal.GoogleCloudBuildProvidersDelegate
import com.kelvsyc.gradle.internal.TeamCityProvidersDelegate
import org.gradle.api.model.ObjectFactory

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to AWS CodeBuild.
 */
val ObjectFactory.awsCodeBuild by AwsCodeBuildProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to Azure Pipelines.
 */
val ObjectFactory.azurePipelines by AzurePipelinesProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to CircleCI.
 */
val ObjectFactory.circleCI by CircleCIProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions.
 */
val ObjectFactory.githubActions by GitHubActionsProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions runners
 * hosted by AWS CodeBuild.
 */
val ObjectFactory.githubCodeBuildActions by GitHubCodeBuildActionsProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitLab CI/CD.
 */
val ObjectFactory.gitlabCI by GitLabCIProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitLab CI/CD merge
 * request pipelines.
 */
val ObjectFactory.gitlabMergeRequest by GitLabCIMergeRequestProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to Google Cloud Build.
 */
val ObjectFactory.googleCloudBuild by GoogleCloudBuildProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to TeamCity.
 */
val ObjectFactory.teamCity by TeamCityProvidersDelegate
