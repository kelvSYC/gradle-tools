package com.kelvsyc.gradle.azure.functions

/**
 * Authentication level required by an Azure Functions HTTP trigger, as configured on the function
 * app in Azure Resource Manager.
 *
 * This reflects the value reported by the ARM API and describes what the *function* requires —
 * independent of [FunctionAuthMode], which describes what the *caller* provides.
 */
enum class FunctionAuthLevel {
    /** No authentication required; the function endpoint is publicly accessible. */
    ANONYMOUS,

    /** A function-specific or host key must be supplied via the `x-functions-key` header. */
    FUNCTION,

    /** The function app master key must be supplied. */
    ADMIN,
}
