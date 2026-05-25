package com.kelvsyc.gradle.google.cloud

import com.google.auth.oauth2.ExternalAccountCredentials
import com.google.auth.oauth2.IdentityPoolCredentials
import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.services.BuildServiceSpec
import org.gradle.testfixtures.ProjectBuilder

private val externalAccountJson = """
    {
      "type": "external_account",
      "audience": "//iam.googleapis.com/projects/123456/locations/global/workloadIdentityPools/test-pool/providers/test-provider",
      "subject_token_type": "urn:ietf:params:oauth:token-type:jwt",
      "token_url": "https://sts.googleapis.com/v1/token",
      "credential_source": {
        "file": "/nonexistent"
      }
    }
""".trimIndent()

class AbstractGcpClientBuildServiceSpec : FunSpec({
    test("resolveCredentials - EXTERNAL_ACCOUNT_CONFIG_FILE returns ExternalAccountCredentials") {
        val tmpFile = java.io.File.createTempFile("test-ext-account", ".json")
        try {
            tmpFile.writeText(externalAccountJson)
            val project = ProjectBuilder.builder().build()
            val fileProp = project.objects.fileProperty()
            fileProp.set(tmpFile)

            val service = project.gradle.sharedServices
                .registerIfAbsent("extAccountFile", TestGcpClientBuildService::class.java) { spec: BuildServiceSpec<GcpBuildServiceParams> ->
                    spec.parameters.externalAccount(fileProp)
                }

            service.get().testResolveCredentials().shouldBeInstanceOf<ExternalAccountCredentials>()
        } finally {
            tmpFile.delete()
        }
    }

    test("resolveCredentials - EXTERNAL_ACCOUNT_CONFIG_ENV returns ExternalAccountCredentials") {
        val propName = "test.gcp.external.account.config"
        System.setProperty(propName, externalAccountJson)
        try {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("extAccountEnv", TestGcpClientBuildService::class.java) { spec: BuildServiceSpec<GcpBuildServiceParams> ->
                    spec.parameters.externalAccount(CredentialReference.SystemProperty(propName))
                }

            service.get().testResolveCredentials().shouldBeInstanceOf<ExternalAccountCredentials>()
        } finally {
            System.clearProperty(propName)
        }
    }

    test("resolveCredentials - WORKLOAD_IDENTITY_OIDC without impersonation returns IdentityPoolCredentials") {
        val tokenPropName = "test.gcp.oidc.token"
        System.setProperty(tokenPropName, "fake-oidc-token")
        try {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("workloadIdentity", TestGcpClientBuildService::class.java) { spec: BuildServiceSpec<GcpBuildServiceParams> ->
                    spec.parameters.workloadIdentity(
                        audience = "//iam.googleapis.com/projects/123456/locations/global/workloadIdentityPools/test-pool/providers/test-provider",
                        token = CredentialReference.SystemProperty(tokenPropName),
                    )
                }

            service.get().testResolveCredentials().shouldBeInstanceOf<IdentityPoolCredentials>()
        } finally {
            System.clearProperty(tokenPropName)
        }
    }

    test("resolveCredentials - WORKLOAD_IDENTITY_OIDC with impersonation includes SA email in impersonation URL") {
        val tokenPropName = "test.gcp.oidc.token.imp"
        val saEmail = "my-sa@my-project.iam.gserviceaccount.com"
        System.setProperty(tokenPropName, "fake-oidc-token")
        try {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("workloadIdentityImp", TestGcpClientBuildService::class.java) { spec: BuildServiceSpec<GcpBuildServiceParams> ->
                    spec.parameters.workloadIdentity(
                        audience = "//iam.googleapis.com/projects/123456/locations/global/workloadIdentityPools/test-pool/providers/test-provider",
                        token = CredentialReference.SystemProperty(tokenPropName),
                        impersonateServiceAccount = saEmail,
                    )
                }

            val idpCredentials = service.get().testResolveCredentials()
                .shouldBeInstanceOf<IdentityPoolCredentials>()
            idpCredentials.serviceAccountImpersonationUrl.shouldNotBeNull() shouldContain saEmail
        } finally {
            System.clearProperty(tokenPropName)
        }
    }
})
