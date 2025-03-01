package com.kelvsyc.gradle

import com.kelvsyc.gradle.internal.GitHubActionsProvidersDelegate
import org.gradle.api.model.ObjectFactory

/**
 * Retrieves an object used to create [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions.
 */
val ObjectFactory.githubActions by GitHubActionsProvidersDelegate
