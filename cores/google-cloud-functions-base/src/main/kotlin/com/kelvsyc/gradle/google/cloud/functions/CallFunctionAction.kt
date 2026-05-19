package com.kelvsyc.gradle.google.cloud.functions

import com.kelvsyc.gradle.clients.CredentialReference
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that invokes a Cloud Functions Gen 2 function via HTTP POST.
 *
 * Gen 2 functions are HTTPS endpoints backed by Cloud Run. This action sends a POST request to
 * the function's trigger URI, optionally including an OIDC identity token for authentication and
 * a request payload. The response body is discarded.
 *
 * To obtain the function URI at configuration time, use [GetFunctionValueSource].
 */
abstract class CallFunctionAction : WorkAction<CallFunctionAction.Parameters> {

    /**
     * Parameters for [CallFunctionAction].
     */
    interface Parameters : WorkParameters {
        /** The full HTTPS trigger URI of the function. */
        val uri: Property<String>

        /**
         * Reference to where the OIDC identity token can be found. When present, the resolved
         * token is sent as `Authorization: Bearer <token>`. Uses [CredentialReference] to keep
         * the token out of the Gradle configuration cache.
         */
        val identityToken: Property<CredentialReference>

        /** Optional request body to send as the POST payload. */
        val payload: Property<String>
    }

    override fun execute() {
        val requestBody = parameters.payload.orNull?.toRequestBody() ?: "".toRequestBody()
        val requestBuilder = Request.Builder()
            .url(parameters.uri.get())
            .post(requestBody)
        parameters.identityToken.orNull?.resolve()?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        OkHttpClient().newCall(requestBuilder.build()).execute().close()
    }
}
