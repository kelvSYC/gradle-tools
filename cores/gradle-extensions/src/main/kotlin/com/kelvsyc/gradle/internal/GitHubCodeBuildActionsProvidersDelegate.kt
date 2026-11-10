package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitHubCodeBuildActionsProviders
import org.gradle.api.model.ObjectFactory

object GitHubCodeBuildActionsProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GitHubCodeBuildActionsProviders>({
    it.newInstance(GitHubCodeBuildActionsProviders::class.java)
})
