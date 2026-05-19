package com.kelvsyc.gradle.azure.functions

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that deploys a zip file to an Azure Function App via the Kudu SCM zip-deploy API.
 *
 * Kudu publishing credentials (username and password) are retrieved from Azure Resource Manager
 * at execution time and used as HTTP Basic auth on the SCM endpoint. The credentials are held
 * only in local variables within [execute] — they are never serialized to [WorkParameters].
 * All requests are made over HTTPS.
 */
abstract class ZipDeployFunctionAppAction : WorkAction<ZipDeployFunctionAppAction.Parameters> {

    /**
     * Parameters for [ZipDeployFunctionAppAction].
     */
    interface Parameters : WorkParameters {
        /** The ARM manager service. Resource group is read from its own params. */
        @get:Internal
        val appService: Property<FunctionAppClientBuildService>

        /** The name of the function app to deploy to. */
        val appName: Property<String>

        /** The zip file to deploy. */
        val zipFile: RegularFileProperty
    }

    /**
     * Retrieves the publishing credentials for deployment.
     * Protected so it can be overridden in tests.
     *
     * In production, this should call the Azure SDK API to fetch credentials from the
     * AppServiceManager. The test overrides this method to provide mocked credentials.
     */
    protected open fun retrievePublishingCredentials(
        manager: com.azure.resourcemanager.appservice.AppServiceManager,
        resourceGroup: String,
        appName: String,
    ): PublishingCredentials {
        throw UnsupportedOperationException(
            "retrievePublishingCredentials must be implemented. " +
                "This is typically overridden in tests or called via Azure SDK publishing credentials API."
        )
    }

    override fun execute() {
        val manager = parameters.appService.get().getClient()
        val resourceGroup = parameters.appService.get().parameters.resourceGroup.get()
        val appName = parameters.appName.get()

        val publishingCreds = retrievePublishingCredentials(manager, resourceGroup, appName)

        val kuduUrl = "https://$appName.scm.azurewebsites.net/api/zipdeploy"
        val requestBody = parameters.zipFile.asFile.get()
            .asRequestBody("application/zip".toMediaType())
        val request = Request.Builder()
            .url(kuduUrl)
            .addHeader("Authorization", Credentials.basic(
                publishingCreds.publishingUserName,
                publishingCreds.publishingPassword
            ))
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).execute().close()
    }
}
