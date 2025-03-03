package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitHubCodeBuildActionsProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object GitHubCodeBuildActionsProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GitHubCodeBuildActionsProviders>({
    it.newInstance()
})
