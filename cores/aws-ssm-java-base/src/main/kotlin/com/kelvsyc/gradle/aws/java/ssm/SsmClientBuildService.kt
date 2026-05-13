package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.ssm.SsmClient

/**
 * Build service managing an [SsmClient] instance.
 */
abstract class SsmClientBuildService : AbstractAwsJavaClientBuildService<SsmClient, AwsBuildServiceParams>() {
    override fun createClient(): SsmClient = configureBuilder(SsmClient.builder()).build()
}
