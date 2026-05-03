package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.ecr.EcrClient

interface MockEcrClientInfo : AwsClientInfo<EcrClient>
