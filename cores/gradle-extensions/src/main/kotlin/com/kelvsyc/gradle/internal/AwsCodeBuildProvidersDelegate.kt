package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.AwsCodeBuildProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object AwsCodeBuildProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, AwsCodeBuildProviders>({
        it.newInstance()
    })
