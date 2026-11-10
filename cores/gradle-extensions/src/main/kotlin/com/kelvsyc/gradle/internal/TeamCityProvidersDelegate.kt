package com.kelvsyc.gradle.internal

import com.kelvsyc.gradle.providers.TeamCityProviders
import org.gradle.api.model.ObjectFactory

object TeamCityProvidersDelegate :
    AbstractCachingDelegate<ObjectFactory, TeamCityProviders>({
        it.newInstance(TeamCityProviders::class.java)
    })
