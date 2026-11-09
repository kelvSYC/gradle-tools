package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.CircleCIProviders
import org.gradle.api.model.ObjectFactory

object CircleCIProvidersDelegate : AbstractCachingDelegate<ObjectFactory, CircleCIProviders>({
    it.newInstance(CircleCIProviders::class.java)
})
