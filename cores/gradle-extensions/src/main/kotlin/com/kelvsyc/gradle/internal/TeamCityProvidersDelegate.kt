package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.TeamCityProviders
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

object TeamCityProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, TeamCityProviders>({
        it.newInstance()
    })
