package com.kelvsyc.gradle.aws.java

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.api.Transformer
import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

class GradleSessionCredentialsProviderSpec : FunSpec() {
    init {
        test("resolveCredentials - maps Gradle AwsCredentials to AwsSessionCredentials") {
            val gradleCredentials = mockk<GradleAwsCredentials>()
            every { gradleCredentials.accessKey } returns "ACCESS_KEY"
            every { gradleCredentials.secretKey } returns "SECRET_KEY"
            every { gradleCredentials.sessionToken } returns "SESSION_TOKEN"

            val transformerSlot = slot<Transformer<AwsCredentials, GradleAwsCredentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<GradleAwsCredentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(gradleCredentials) }

            val sut = GradleSessionCredentialsProvider(provider)
            val resolved = sut.resolveCredentials() as AwsSessionCredentials

            resolved.accessKeyId() shouldBeEqual "ACCESS_KEY"
            resolved.secretAccessKey() shouldBeEqual "SECRET_KEY"
            resolved.sessionToken() shouldBeEqual "SESSION_TOKEN"
        }
    }
}
