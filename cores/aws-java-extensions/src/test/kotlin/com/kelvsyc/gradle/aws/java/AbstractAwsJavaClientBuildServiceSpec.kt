package com.kelvsyc.gradle.aws.java

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.services.BuildServiceSpec
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.awscore.AwsClient
import software.amazon.awssdk.regions.Region

class AbstractAwsJavaClientBuildServiceSpec : FunSpec() {
    abstract class TestService : AbstractAwsJavaClientBuildService<AwsClient, AwsBuildServiceParams>() {
        fun testResolveRegion() = resolveRegion()
        fun testResolveCredentialsProvider() = resolveCredentialsProvider()
        override fun createClient(): AwsClient = error("not used in tests")
    }

    init {
        test("resolveRegion - returns null when regionId is absent") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java)

            service.get().testResolveRegion().shouldBeNull()
        }

        test("resolveRegion - returns Region when regionId is set") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.regionId.set("us-east-1")
            }

            service.get().testResolveRegion() shouldBe Region.US_EAST_1
        }

        test("resolveCredentialsProvider - returns AnonymousCredentialsProvider when credentialSource is absent") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java)

            service.get().testResolveCredentialsProvider().shouldBeInstanceOf<AnonymousCredentialsProvider>()
        }

        test("resolveCredentialsProvider - returns AnonymousCredentialsProvider when credentialSource is ANONYMOUS") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.ANONYMOUS)
            }

            service.get().testResolveCredentialsProvider().shouldBeInstanceOf<AnonymousCredentialsProvider>()
        }

        test("resolveCredentialsProvider - returns DefaultCredentialsProvider when credentialSource is DEFAULT_CHAIN") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.DEFAULT_CHAIN)
            }

            service.get().testResolveCredentialsProvider().shouldBeInstanceOf<DefaultCredentialsProvider>()
        }

        test("resolveCredentialsProvider - returns StaticCredentialsProvider with AwsBasicCredentials when STATIC and no sessionToken") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.STATIC)
                spec.parameters.accessKeyId.set("AKID")
                spec.parameters.secretAccessKey.set("SECRET")
            }

            val provider = service.get().testResolveCredentialsProvider()
            provider.shouldBeInstanceOf<StaticCredentialsProvider>()
            val creds = provider.resolveCredentials() as AwsBasicCredentials
            creds.accessKeyId() shouldBe "AKID"
            creds.secretAccessKey() shouldBe "SECRET"
        }

        test("resolveCredentialsProvider - returns StaticCredentialsProvider with AwsSessionCredentials when STATIC with sessionToken") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.STATIC)
                spec.parameters.accessKeyId.set("AKID")
                spec.parameters.secretAccessKey.set("SECRET")
                spec.parameters.sessionToken.set("TOKEN")
            }

            val provider = service.get().testResolveCredentialsProvider()
            provider.shouldBeInstanceOf<StaticCredentialsProvider>()
            val creds = provider.resolveCredentials() as AwsSessionCredentials
            creds.accessKeyId() shouldBe "AKID"
            creds.secretAccessKey() shouldBe "SECRET"
            creds.sessionToken() shouldBe "TOKEN"
        }

        test("resolveCredentialsProvider - returns ProfileCredentialsProvider when credentialSource is PROFILE") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.PROFILE)
                spec.parameters.credentialsProfile.set("my-profile")
            }

            service.get().testResolveCredentialsProvider().shouldBeInstanceOf<ProfileCredentialsProvider>()
        }
    }
}
