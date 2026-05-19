package com.kelvsyc.gradle.azure.functions

/**
 * Authentication mode chosen by the caller when submitting a [CallFunctionAction] invocation.
 *
 * This is independent of [FunctionAuthLevel]: [FunctionAuthLevel] describes what the function
 * requires; [FunctionAuthMode] describes what the caller provides. A FUNCTION-level function
 * can still be called with an Azure AD token if the app is also configured for AAD authentication.
 */
enum class FunctionAuthMode {
    /** No authentication credentials are sent. Suitable for [FunctionAuthLevel.ANONYMOUS] functions. */
    ANONYMOUS,

    /** A function or host key is sent as the `x-functions-key` header. */
    FUNCTION_KEY,

    /** An Azure AD Bearer token is sent as the `Authorization: Bearer` header. */
    AZURE_AD,
}
