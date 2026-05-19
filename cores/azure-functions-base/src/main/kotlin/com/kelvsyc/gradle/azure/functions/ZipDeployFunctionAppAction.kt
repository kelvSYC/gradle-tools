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

    override fun execute() {
        val manager = parameters.appService.get().getClient()
        val resourceGroup = parameters.appService.get().parameters.resourceGroup.get()
        val appName = parameters.appName.get()
        val functionApp = manager.functionApps().getByResourceGroup(resourceGroup, appName)

        // Retrieve publishing credentials from the function app
        @Suppress("UNCHECKED_CAST")
        val publishingProfiles = functionApp.javaClass.getDeclaredMethod("getPublishingProfiles")
            .invoke(functionApp) as List<Any>
        val publishingProfile = publishingProfiles.firstOrNull()
            ?: throw IllegalStateException("No publishing profiles found for function app '$appName'")

        val publishingUserName = publishingProfile.javaClass.getMethod("publishingUserName")
            .invoke(publishingProfile) as String
        val publishingPassword = publishingProfile.javaClass.getMethod("publishingPassword")
            .invoke(publishingProfile) as String

        val kuduUrl = "https://$appName.scm.azurewebsites.net/api/zipdeploy"
        val requestBody = parameters.zipFile.asFile.get()
            .asRequestBody("application/zip".toMediaType())
        val request = Request.Builder()
            .url(kuduUrl)
            .addHeader("Authorization", Credentials.basic(
                publishingUserName,
                publishingPassword
            ))
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).execute().close()
    }
}
