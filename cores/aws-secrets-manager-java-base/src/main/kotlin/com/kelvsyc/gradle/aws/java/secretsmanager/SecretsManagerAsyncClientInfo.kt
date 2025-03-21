package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient

interface SecretsManagerAsyncClientInfo : AwsClientInfo<SecretsManagerAsyncClient>
