package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitHubActionsProviders
import org.gradle.api.model.ObjectFactory

object GitHubActionsProvidersDelegate : AbstractCachingDelegate<ObjectFactory, GitHubActionsProviders>({
    it.newInstance(GitHubActionsProviders::class.java)
})
