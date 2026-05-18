/**
 * Azure Container Registry client and Gradle build service integration.
 *
 * Provides build services for managing [com.azure.containers.containerregistry.ContainerRegistryClient]
 * and [com.azure.containers.containerregistry.ContainerRegistryAsyncClient] instances, scoped to a
 * single ACR registry endpoint. Both services support [com.azure.core.credential.TokenCredential]
 * authentication (DefaultAzureCredential, ManagedIdentity, ClientSecret) but not SAS or named
 * key credentials.
 */
package com.kelvsyc.gradle.azure.containerregistry
