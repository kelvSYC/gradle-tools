package com.kelvsyc.gradle.aws.java

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.api.Transformer
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials

class GradleCredentialsProvidersSpec : FunSpec() {
    init {
        test("resolveCredentials - maps username and password to access key and secret") {
            val passwordCredentials = mockk<PasswordCredentials>()
            every { passwordCredentials.username } returns "ACCESS_KEY"
            every { passwordCredentials.password } returns "SECRET_KEY"

            val transformerSlot = slot<Transformer<AwsCredentials, PasswordCredentials>>()
            val mappedProvider = mockk<Provider<AwsCredentials>>()

            val provider = mockk<Provider<PasswordCredentials>>()
            every { provider.map<AwsCredentials>(capture(transformerSlot)) } returns mappedProvider
            every { mappedProvider.get() } answers { transformerSlot.captured.transform(passwordCredentials) }

            val sut = GradleCredentialsProviders(provider)
            val resolved = sut.resolveCredentials() as AwsBasicCredentials

            resolved.accessKeyId() shouldBeEqual "ACCESS_KEY"
            resolved.secretAccessKey() shouldBeEqual "SECRET_KEY"
        }
    }
}
