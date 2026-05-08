package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitLabCIProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object GitLabCIProvidersDelegate : AbstractCachingDelegate<ObjectFactory, GitLabCIProviders>({
    it.newInstance()
})
