package com.kelvsyc.gradle.aws.java.appconfig

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.appconfig.AppConfigClient

/**
 * Build service managing an [AppConfigClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams] with the desired AWS region and credentials.
 */
abstract class AppConfigClientBuildService :
    AbstractAwsJavaClientBuildService<AppConfigClient, AwsBuildServiceParams>() {
    override fun createClient(): AppConfigClient = configureBuilder(AppConfigClient.builder()).build()
}
