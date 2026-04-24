package com.kelvsyc.gradle.aws.kotlin

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.api.Transformer
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.provider.Provider

class CredentialsProviderExtensionsSpec : FunSpec() {
    init {
        test("asCredentialsProvider - maps Gradle AwsCredentials to StaticCredentialsProvider") {
            val gradleCredentials = mockk<AwsCredentials>()
            every { gradleCredentials.accessKey } returns "ACCESS_KEY"
            every { gradleCredentials.secretKey } returns "SECRET_KEY"
            every { gradleCredentials.sessionToken } returns "SESSION_TOKEN"

            val transformerSlot = slot<Transformer<CredentialsProvider, AwsCredentials>>()
            val mappedProvider = mockk<Provider<CredentialsProvider>>()

            val provider = mockk<Provider<AwsCredentials>>()
            every { provider.map<CredentialsProvider>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(gradleCredentials) }

            val result = provider.asCredentialsProvider.get()

            result.shouldBeInstanceOf<StaticCredentialsProvider>()
        }

        test("asCredentialsProvider - passes null session token when absent") {
            val gradleCredentials = mockk<AwsCredentials>()
            every { gradleCredentials.accessKey } returns "ACCESS_KEY"
            every { gradleCredentials.secretKey } returns "SECRET_KEY"
            every { gradleCredentials.sessionToken } returns null

            val transformerSlot = slot<Transformer<CredentialsProvider, AwsCredentials>>()
            val mappedProvider = mockk<Provider<CredentialsProvider>>()

            val provider = mockk<Provider<AwsCredentials>>()
            every { provider.map<CredentialsProvider>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(gradleCredentials) }

            val result = provider.asCredentialsProvider.get()

            result.shouldBeInstanceOf<StaticCredentialsProvider>()
        }

        test("asGradleCredentials - maps AWS Credentials to Gradle AwsCredentials") {
            val awsCredentials = Credentials("ACCESS_KEY", "SECRET_KEY", "SESSION_TOKEN")

            val transformerSlot = slot<Transformer<AwsCredentials, Credentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<Credentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(awsCredentials) }

            val result = provider.asGradleCredentials.get()

            result.accessKey!! shouldBeEqual "ACCESS_KEY"
            result.secretKey!! shouldBeEqual "SECRET_KEY"
            result.sessionToken!! shouldBeEqual "SESSION_TOKEN"
        }

        test("asGradleCredentials - maps AWS Credentials with null session token") {
            val awsCredentials = Credentials("ACCESS_KEY", "SECRET_KEY")

            val transformerSlot = slot<Transformer<AwsCredentials, Credentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<Credentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(awsCredentials) }

            val result = provider.asGradleCredentials.get()

            result.accessKey!! shouldBeEqual "ACCESS_KEY"
            result.secretKey!! shouldBeEqual "SECRET_KEY"
            result.sessionToken.shouldBeNull()
        }

        test("asGradleCredentials - setAccessKey throws UnsupportedOperationException") {
            val awsCredentials = Credentials("ACCESS_KEY", "SECRET_KEY")

            val transformerSlot = slot<Transformer<AwsCredentials, Credentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<Credentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(awsCredentials) }

            val result = provider.asGradleCredentials.get()

            shouldThrow<UnsupportedOperationException> { result.setAccessKey("NEW_KEY") }
        }

        test("asGradleCredentials - setSecretKey throws UnsupportedOperationException") {
            val awsCredentials = Credentials("ACCESS_KEY", "SECRET_KEY")

            val transformerSlot = slot<Transformer<AwsCredentials, Credentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<Credentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(awsCredentials) }

            val result = provider.asGradleCredentials.get()

            shouldThrow<UnsupportedOperationException> { result.setSecretKey("NEW_SECRET") }
        }

        test("asGradleCredentials - setSessionToken throws UnsupportedOperationException") {
            val awsCredentials = Credentials("ACCESS_KEY", "SECRET_KEY")

            val transformerSlot = slot<Transformer<AwsCredentials, Credentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<Credentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(awsCredentials) }

            val result = provider.asGradleCredentials.get()

            shouldThrow<UnsupportedOperationException> { result.setSessionToken("NEW_TOKEN") }
        }
    }
}

