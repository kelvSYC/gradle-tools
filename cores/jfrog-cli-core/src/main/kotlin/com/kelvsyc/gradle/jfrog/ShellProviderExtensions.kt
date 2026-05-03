package com.kelvsyc.gradle.jfrog

import org.gradle.api.provider.ProviderFactory

internal fun ProviderFactory.which(command: String) = exec {
    executable("which")
    args(listOf(command))
}.standardOutput.asText.map { it.trim() }
