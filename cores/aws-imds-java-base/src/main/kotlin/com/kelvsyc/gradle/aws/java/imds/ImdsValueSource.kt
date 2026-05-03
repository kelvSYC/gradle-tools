package com.kelvsyc.gradle.aws.java.imds

import software.amazon.awssdk.imds.Ec2MetadataResponse

/**
 * [AbstractImdsValueSource] implementation returning the raw IMDS response as a string.
 *
 * Useful for simple metadata lookups such as instance ID, AMI ID, or availability zone.
 */
abstract class ImdsValueSource : AbstractImdsValueSource<String, AbstractImdsValueSource.Parameters>() {
    override fun doObtain(response: Ec2MetadataResponse): String = response.asString()
}
