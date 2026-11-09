package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.GoogleCloudBuildProviders
import org.gradle.api.model.ObjectFactory

object GoogleCloudBuildProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, GoogleCloudBuildProviders>({
        it.newInstance(GoogleCloudBuildProviders::class.java)
    })
