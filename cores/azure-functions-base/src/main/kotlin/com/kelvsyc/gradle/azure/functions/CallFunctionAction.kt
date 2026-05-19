package com.kelvsyc.gradle.azure.functions

import com.kelvsyc.gradle.clients.CredentialReference
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that invokes an Azure Functions HTTP trigger via POST.
 *
 * Auth is sent via the `x-functions-key` header (for [FunctionAuthMode.FUNCTION_KEY]) or
 * `Authorization: Bearer` (for [FunctionAuthMode.AZURE_AD]) — never as a query parameter,
 * to avoid credentials appearing in server access logs. HTTPS is enforced; plain HTTP URIs
 * are rejected before any network call is made.
 *
 * Use the convenience extensions [anonymous], [functionKey], and [azureAdToken] on [Parameters]
 * to configure the auth mode and credential together in one call.
 */
abstract class CallFunctionAction : WorkAction<CallFunctionAction.Parameters> {

    /**
     * Parameters for [CallFunctionAction].
     */
    interface Parameters : WorkParameters {
        /** The full HTTPS trigger URI of the function. */
        val uri: Property<String>

        /**
         * The authentication mode to use when calling the function.
         * Use [anonymous], [functionKey], or [azureAdToken] to set this together with the
         * corresponding credential reference.
         */
        val authMode: Property<FunctionAuthMode>

        /**
         * Reference to the function or host key. Required when [authMode] is [FunctionAuthMode.FUNCTION_KEY].
         */
        @get:Internal
        val functionKeyRef: Property<CredentialReference>

        /**
         * Reference to the Azure AD Bearer token. Required when [authMode] is [FunctionAuthMode.AZURE_AD].
         */
        @get:Internal
        val identityTokenRef: Property<CredentialReference>

        /** Optional request body to include in the POST. If unset, defaults to an empty body. */
        val payload: Property<String>
    }

    override fun execute() {
        val uri = parameters.uri.get()
        require(uri.startsWith("https://")) { "CallFunctionAction requires an HTTPS URI; got: $uri" }

        val requestBody = parameters.payload.orNull?.toRequestBody() ?: "".toRequestBody()
        val requestBuilder = Request.Builder()
            .url(uri)
            .post(requestBody)

        when (parameters.authMode.getOrElse(FunctionAuthMode.ANONYMOUS)) {
            FunctionAuthMode.ANONYMOUS -> Unit
            FunctionAuthMode.FUNCTION_KEY ->
                requestBuilder.header("x-functions-key", parameters.functionKeyRef.get().resolve())
            FunctionAuthMode.AZURE_AD ->
                requestBuilder.header("Authorization", "Bearer ${parameters.identityTokenRef.get().resolve()}")
        }

        OkHttpClient().newCall(requestBuilder.build()).execute().close()
    }
}

/** Sets [CallFunctionAction.Parameters.authMode] to [FunctionAuthMode.ANONYMOUS]. */
fun CallFunctionAction.Parameters.anonymous() {
    authMode.set(FunctionAuthMode.ANONYMOUS)
}

/**
 * Sets [CallFunctionAction.Parameters.authMode] to [FunctionAuthMode.FUNCTION_KEY] and stores
 * the function/host key reference. The key is resolved at execution time — not serialized.
 */
fun CallFunctionAction.Parameters.functionKey(ref: CredentialReference) {
    authMode.set(FunctionAuthMode.FUNCTION_KEY)
    functionKeyRef.set(ref)
}

/**
 * Sets [CallFunctionAction.Parameters.authMode] to [FunctionAuthMode.AZURE_AD] and stores
 * the Bearer token reference. The token is resolved at execution time — not serialized.
 */
fun CallFunctionAction.Parameters.azureAdToken(ref: CredentialReference) {
    authMode.set(FunctionAuthMode.AZURE_AD)
    identityTokenRef.set(ref)
}
