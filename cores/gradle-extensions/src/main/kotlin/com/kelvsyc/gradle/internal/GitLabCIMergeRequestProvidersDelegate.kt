package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitLabCIMergeRequestProviders
import org.gradle.api.model.ObjectFactory

object GitLabCIMergeRequestProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GitLabCIMergeRequestProviders>({
        it.newInstance(GitLabCIMergeRequestProviders::class.java)
    })
