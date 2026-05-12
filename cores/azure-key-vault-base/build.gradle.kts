import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Azure Key Vault Base")
}

gradlePlugin {
    plugins.register("azure-key-vault-base") {
        id = "com.kelvsyc.gradle.azure-key-vault-base"
        implementationClass = "com.kelvsyc.gradle.plugins.AzureKeyVaultBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.azure.core)
    api(libs.azure.security.keyvault.secrets)

    testImplementation(libs.mockk)
}
