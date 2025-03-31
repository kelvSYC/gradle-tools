package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.ses.SesClient

interface SesClientInfo : AwsClientInfo<SesClient>
