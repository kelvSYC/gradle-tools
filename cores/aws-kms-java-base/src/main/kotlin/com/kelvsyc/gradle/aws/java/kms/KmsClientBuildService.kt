package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.kms.KmsClient

/**
 * Build service managing a [KmsClient] instance.
 */
abstract class KmsClientBuildService : AbstractAwsJavaClientBuildService<KmsClient, AwsBuildServiceParams>() {
    override fun createClient(): KmsClient = configureBuilder(KmsClient.builder()).build()
}
