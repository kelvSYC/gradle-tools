package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] providing the invoke URL template for the function scoped by the parent
 * [FunctionClientBuildService], or `null` if the function is not found.
 *
 * The URL template may contain a `{code}` placeholder — this is a structural part of the URL,
 * not a resolved secret. Safe for the Gradle configuration cache.
 */
abstract class GetFunctionValueSource :
    ValueSource<String, GetFunctionValueSource.Parameters> {

    /**
     * Parameters for [GetFunctionValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The function-scoped build service providing [FunctionInfo] for the target function.
         */
        @get:Internal
        val functionService: Property<FunctionClientBuildService>
    }

    override fun obtain(): String? =
        parameters.functionService.get().getClient().invokeUrlTemplate.takeIf { it.isNotBlank() }
}
