package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.AwsCodeBuildProviders
import org.gradle.api.model.ObjectFactory

object AwsCodeBuildProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, AwsCodeBuildProviders>({
        it.newInstance(AwsCodeBuildProviders::class.java)
    })
