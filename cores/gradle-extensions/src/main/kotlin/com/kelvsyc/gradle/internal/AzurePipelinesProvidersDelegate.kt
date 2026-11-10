package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.AzurePipelinesProviders
import org.gradle.api.model.ObjectFactory

object AzurePipelinesProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, AzurePipelinesProviders>({
        it.newInstance(AzurePipelinesProviders::class.java)
    })
