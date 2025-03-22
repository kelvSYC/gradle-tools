package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface SecretsManagerClientInfo : AwsClientInfo<SecretsManagerClient>
