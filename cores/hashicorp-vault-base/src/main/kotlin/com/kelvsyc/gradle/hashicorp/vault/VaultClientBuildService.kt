package com.kelvsyc.gradle.hashicorp.vault

/**
 * A concrete [AbstractVaultClientBuildService] using [VaultBuildServiceParams].
 *
 * Register this service with [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent]:
 *
 * ```kotlin
 * val vault = gradle.sharedServices.registerIfAbsent("vault", VaultClientBuildService::class) {
 *     parameters {
 *         endpoint.set("https://vault.example.com:8200")
 *         tokenAuth()
 *     }
 * }
 * ```
 *
 * Inject the service into your own [org.gradle.workers.WorkAction] to access credentials
 * at task execution time:
 *
 * ```kotlin
 * abstract class MyWorkAction : WorkAction<MyWorkAction.Parameters> {
 *     interface Parameters : WorkParameters {
 *         @get:Internal
 *         val vaultService: Property<VaultClientBuildService>
 *     }
 *
 *     override fun execute() {
 *         val secret = parameters.vaultService.get().getKvSecret("secret/myapp", "apiKey")
 *         // use secret...
 *     }
 * }
 * ```
 */
abstract class VaultClientBuildService : AbstractVaultClientBuildService<VaultBuildServiceParams>()
