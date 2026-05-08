package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.AzurePipelinesProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object AzurePipelinesProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, AzurePipelinesProviders>({
        it.newInstance()
    })
