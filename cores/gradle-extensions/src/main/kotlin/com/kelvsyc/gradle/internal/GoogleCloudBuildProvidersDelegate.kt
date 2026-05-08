package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GoogleCloudBuildProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object GoogleCloudBuildProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GoogleCloudBuildProviders>({
        it.newInstance()
    })
