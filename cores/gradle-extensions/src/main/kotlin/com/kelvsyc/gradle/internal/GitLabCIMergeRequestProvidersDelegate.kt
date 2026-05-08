package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GitLabCIMergeRequestProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object GitLabCIMergeRequestProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GitLabCIMergeRequestProviders>({
        it.newInstance()
    })
