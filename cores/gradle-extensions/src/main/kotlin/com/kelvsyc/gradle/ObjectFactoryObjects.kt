package com.kelvsyc.gradle

import com.kelvsyc.gradle.internal.AwsCodeBuildProvidersDelegate
import com.kelvsyc.gradle.internal.GitHubActionsProvidersDelegate
import com.kelvsyc.gradle.internal.GitHubCodeBuildActionsProvidersDelegate
import org.gradle.api.model.ObjectFactory

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to AWS CodeBuild.
 */
val ObjectFactory.awsCodeBuild by AwsCodeBuildProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions.
 */
val ObjectFactory.githubActions by GitHubActionsProvidersDelegate

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions runners
 * hosted by AWS CodeBuild.
 */
val ObjectFactory.githubCodeBuildActions by GitHubCodeBuildActionsProvidersDelegate
