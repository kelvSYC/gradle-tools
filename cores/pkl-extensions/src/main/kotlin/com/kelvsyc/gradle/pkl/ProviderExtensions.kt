package com.kelvsyc.gradle.pkl

import org.gradle.api.provider.Provider
import org.pkl.core.Evaluator
import org.pkl.core.ModuleSource
import org.pkl.core.PModule

/**
 * Returns a [Provider] that lazily evaluates this string provider's value as a Pkl [PModule].
 *
 * Each call creates a fresh [Evaluator] with default security settings. Since text-based
 * modules have no base path, relative imports within the Pkl content will not resolve.
 */
fun Provider<String>.parsePkl(): Provider<PModule> = map { text ->
    Evaluator.preconfigured().use { evaluator ->
        evaluator.evaluate(ModuleSource.text(text))
    }
}
