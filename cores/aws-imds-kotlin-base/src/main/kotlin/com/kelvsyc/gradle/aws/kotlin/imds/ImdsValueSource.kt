package com.kelvsyc.gradle.aws.kotlin.imds

/**
 * [AbstractImdsValueSource] implementation returning the raw IMDS response as a string.
 *
 * Useful for simple metadata lookups such as instance ID, AMI ID, or availability zone.
 */
abstract class ImdsValueSource : AbstractImdsValueSource<String, AbstractImdsValueSource.Parameters>() {
    override fun doObtain(response: String): String = response
}
