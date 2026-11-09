package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitLabCIProviders
import org.gradle.api.model.ObjectFactory

object GitLabCIProvidersDelegate : AbstractCachingDelegate<ObjectFactory, GitLabCIProviders>({
    it.newInstance(GitLabCIProviders::class.java)
})
