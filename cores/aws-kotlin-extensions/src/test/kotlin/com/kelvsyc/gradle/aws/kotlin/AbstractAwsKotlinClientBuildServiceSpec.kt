package com.kelvsyc.gradle.aws.kotlin

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.client.SdkClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.gradle.api.services.BuildServiceSpec
import org.gradle.testfixtures.ProjectBuilder

class AbstractAwsKotlinClientBuildServiceSpec : FunSpec() {
    abstract class TestService : AbstractAwsKotlinClientBuildService<SdkClient, AwsBuildServiceParams>() {
        fun testResolveRegion() = resolveRegion()
        fun testResolveCredentialsProvider() = resolveCredentialsProvider()
        override fun createClient(): SdkClient = error("not used in tests")
    }

    init {
        test("resolveRegion - returns null when region is absent") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java)

            service.get().testResolveRegion().shouldBeNull()
        }

        test("resolveRegion - returns the region identifier when set") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.region.set("us-east-1")
            }

            service.get().testResolveRegion() shouldBe "us-east-1"
        }

        test("resolveCredentialsProvider - returns null when credentialSource is absent") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java)

            service.get().testResolveCredentialsProvider().shouldBeNull()
        }

        test("resolveCredentialsProvider - returns null when credentialSource is ANONYMOUS") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.ANONYMOUS)
            }

            service.get().testResolveCredentialsProvider().shouldBeNull()
        }

        test("resolveCredentialsProvider - returns DefaultChainCredentialsProvider when credentialSource is DEFAULT_CHAIN") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.DEFAULT_CHAIN)
            }

            service.get().testResolveCredentialsProvider().shouldBeInstanceOf<DefaultChainCredentialsProvider>()
        }

        test("resolveCredentialsProvider - returns StaticCredentialsProvider with basic Credentials when STATIC and no sessionToken") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.STATIC)
                spec.parameters.accessKeyId.set("AKID")
                spec.parameters.secretAccessKey.set("SECRET")
            }

            val provider = service.get().testResolveCredentialsProvider()
            provider.shouldBeInstanceOf<StaticCredentialsProvider>()
            val creds = runBlocking { provider.resolve() }
            creds.accessKeyId shouldBe "AKID"
            creds.secretAccessKey shouldBe "SECRET"
            creds.sessionToken.shouldBeNull()
        }

        test("resolveCredentialsProvider - returns StaticCredentialsProvider with session Credentials when STATIC with sessionToken") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices.registerIfAbsent("test", TestService::class.java) { spec: BuildServiceSpec<AwsBuildServiceParams> ->
                spec.parameters.credentialSource.set(AwsCredentialSource.STATIC)
                spec.parameters.accessKeyId.set("AKID")
                spec.parameters.secretAccessKey.set("SECRET")
                spec.parameters.sessionToken.set("TOKEN")
            }

            val provider = service.get().testResolveCredentialsProvider()
            provider.shouldBeInstanceOf<StaticCredentialsProvider>()
            val creds = runBlocking { provider.resolve() }
            creds.accessKeyId shouldBe "AKID"
            creds.secretAccessKey shouldBe "SECRET"
            creds.sessionToken shouldBe "TOKEN"
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
