package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitHubActionsProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object GitHubActionsProvidersDelegate : AbstractCachingDelegate<ObjectFactory, GitHubActionsProviders>({
    it.newInstance()
})
