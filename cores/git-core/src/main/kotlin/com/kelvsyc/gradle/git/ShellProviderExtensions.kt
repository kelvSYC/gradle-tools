package com.kelvsyc.gradle.git

import org.gradle.api.provider.ProviderFactory

fun ProviderFactory.which(command: String) = exec {
    executable("which")
    args(listOf(command))
}.standardOutput.asText.map { it.trim() }
