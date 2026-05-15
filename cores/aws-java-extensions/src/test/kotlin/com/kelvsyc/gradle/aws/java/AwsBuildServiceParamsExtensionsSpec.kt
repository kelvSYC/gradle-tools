package com.kelvsyc.gradle.aws.java

import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

class AwsBuildServiceParamsExtensionsSpec : FunSpec() {
    init {
        test("anonymous - sets credentialSource to ANONYMOUS") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            params.anonymous()

            params.credentialSource.get() shouldBe AwsCredentialSource.ANONYMOUS
        }

        test("defaultCredentials - sets credentialSource to DEFAULT_CHAIN") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            params.defaultCredentials()

            params.credentialSource.get() shouldBe AwsCredentialSource.DEFAULT_CHAIN
        }

        test("staticCredentials - sets credentialSource to STATIC and maps accessKey and secretKey") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            params.staticCredentials(
                CredentialReference.Literal("AKID"),
                CredentialReference.Literal("SECRET"),
            )

            params.credentialSource.get() shouldBe AwsCredentialSource.STATIC
            params.accessKeyIdRef.get() shouldBe CredentialReference.Literal("AKID")
            params.secretAccessKeyRef.get() shouldBe CredentialReference.Literal("SECRET")
            params.sessionTokenRef.isPresent.shouldBeFalse()
        }

        test("sessionCredentials - sets credentialSource to STATIC and maps all three fields") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            params.sessionCredentials(
                CredentialReference.Literal("AKID"),
                CredentialReference.Literal("SECRET"),
                CredentialReference.Literal("TOKEN"),
            )

            params.credentialSource.get() shouldBe AwsCredentialSource.STATIC
            params.accessKeyIdRef.get() shouldBe CredentialReference.Literal("AKID")
            params.secretAccessKeyRef.get() shouldBe CredentialReference.Literal("SECRET")
            params.sessionTokenRef.get() shouldBe CredentialReference.Literal("TOKEN")
        }

        test("profileCredentials - sets credentialSource to PROFILE and sets credentialsProfile") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            params.profileCredentials("my-profile")

            params.credentialSource.get() shouldBe AwsCredentialSource.PROFILE
            params.credentialsProfile.get() shouldBe "my-profile"
        }

        test("from PasswordCredentials - sets STATIC and maps username and password") {
            val project = ProjectBuilder.builder().build()
            val creds = mockk<PasswordCredentials>()
            every { creds.username } returns "AKID"
            every { creds.password } returns "SECRET"
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            @Suppress("DEPRECATION")
            params.from(project.provider { creds })

            params.credentialSource.get() shouldBe AwsCredentialSource.STATIC
            params.accessKeyIdRef.get() shouldBe CredentialReference.Literal("AKID")
            params.secretAccessKeyRef.get() shouldBe CredentialReference.Literal("SECRET")
            params.sessionTokenRef.isPresent.shouldBeFalse()
        }

        test("from GradleAwsCredentials - sets STATIC and maps all fields when sessionToken is present") {
            val project = ProjectBuilder.builder().build()
            val creds = mockk<GradleAwsCredentials>()
            every { creds.accessKey } returns "AKID"
            every { creds.secretKey } returns "SECRET"
            every { creds.sessionToken } returns "TOKEN"
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            @Suppress("DEPRECATION")
            params.from(project.provider { creds })

            params.credentialSource.get() shouldBe AwsCredentialSource.STATIC
            params.accessKeyIdRef.get() shouldBe CredentialReference.Literal("AKID")
            params.secretAccessKeyRef.get() shouldBe CredentialReference.Literal("SECRET")
            params.sessionTokenRef.get() shouldBe CredentialReference.Literal("TOKEN")
        }

        test("from GradleAwsCredentials - leaves sessionToken absent when sessionToken is null") {
            val project = ProjectBuilder.builder().build()
            val creds = mockk<GradleAwsCredentials>()
            every { creds.accessKey } returns "AKID"
            every { creds.secretKey } returns "SECRET"
            every { creds.sessionToken } returns null
            val params = project.objects.newInstance(AwsBuildServiceParams::class.java)
            @Suppress("DEPRECATION")
            params.from(project.provider { creds })

            params.credentialSource.get() shouldBe AwsCredentialSource.STATIC
            params.sessionTokenRef.isPresent.shouldBeFalse()
        }
    }
}
