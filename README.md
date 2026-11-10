# gradle-tools

A composite build of Gradle plugins and Kotlin libraries for cloud service integration, build
tooling, and plugin development. Published to GitHub Packages under the group `com.kelvsyc.gradle`.

## Component types

Every published component lives under `cores/` and falls into one of three categories:

### Cores

Standalone Gradle plugins that solve a specific build problem. Apply them directly in your
`build.gradle.kts`:

| Component | Plugin ID | Description | Minimum Gradle |
|-----------|-----------|-------------|--------------|
| `dokka-dash` | `com.kelvsyc.gradle.dokka-dash` | Packages Dokka HTML output into Dash docset bundles | 8.14 |
| `git-core` | `com.kelvsyc.gradle.git-core` | Git archival tasks and JGit extensions for remote repositories | 8.14 |
| `jfrog-cli-core` | `com.kelvsyc.gradle.jfrog-cli-core` | Gradle tasks wrapping the JFrog CLI | 8.14 |
| `karakum-core` | `com.kelvsyc.gradle.karakum-core` | Karakum TypeScript → Kotlin external-declaration codegen, with automatic Kotlin/JS and KMP source-set wiring | 8.14 |
| `aws-lambda-jvm-plugin` | `com.kelvsyc.gradle.aws-lambda-jvm` | Composable JVM Lambda packaging plugins: fat-JAR and thin-JAR deployment ZIPs | 8.14 |
| `aws-lambda-jvm-deploy-plugin` | `com.kelvsyc.gradle.aws-lambda-jvm.deploy` | Wires the `aws-lambda-jvm-plugin` deployment ZIP to an AWS Lambda upload task | 8.14 |

All plugin IDs are prefixed with `com.kelvsyc.gradle.`.

### Extensions

Kotlin libraries to add as dependencies when writing your own Gradle plugins or build logic.
All are published under the group `com.kelvsyc.gradle`.

#### Client extensions

The shared client infrastructure is provided by **`clients-base`**
(`com.kelvsyc.gradle:clients-base`). End users typically do not depend on this library
directly; it is pulled in as a dependency by the service-specific client extensions.

**AWS** client extensions come in **Java SDK** and **Kotlin SDK** variants with mirrored APIs,
since the two AWS SDKs are distinct libraries:

| AWS Service | Java | Kotlin | Minimum Gradle (Java / Kotlin) |
|-------------|------|--------|--------------|
| AppConfig | `aws-appconfig-java-base` | `aws-appconfig-kotlin-base` | 8.14 / 9.0¹ |
| S3 | `aws-s3-java-base` | `aws-s3-kotlin-base` | 8.14 / 9.0¹ |
| SQS | `aws-sqs-java-base` | `aws-sqs-kotlin-base` | 8.14 / 9.0¹ |
| SNS | `aws-sns-java-base` | `aws-sns-kotlin-base` | 8.14 / 9.0¹ |
| SES | `aws-ses-java-base` | `aws-ses-kotlin-base` | 8.14 / 9.0¹ |
| Secrets Manager | `aws-secrets-manager-java-base` | `aws-secrets-manager-kotlin-base` | 8.14 / 9.0¹ |
| CodeArtifact | `aws-codeartifact-java-base` | `aws-codeartifact-kotlin-base` | 8.14 / 9.0¹ |
| ECR | `aws-ecr-java-base` | `aws-ecr-kotlin-base` | 8.14 / 9.0¹ |
| IMDS | `aws-imds-java-base` | `aws-imds-kotlin-base` | 8.14 / 9.0¹ |
| KMS | `aws-kms-java-base` | `aws-kms-kotlin-base` | 8.14 / 9.0¹ |
| Lambda | `aws-lambda-java-base` | `aws-lambda-kotlin-base` | 8.14 / 9.0¹ |
| SSM Parameter Store | `aws-ssm-java-base` | `aws-ssm-kotlin-base` | 8.14 / 9.0¹ |
| STS | `aws-sts-java-base` | `aws-sts-kotlin-base` | 8.14 / 9.0¹ |

**Other** client extensions:

| Component | Description | Minimum Gradle |
|-----------|-------------|--------------|
| `google-cloud-artifact-registry-base` | GCP Artifact Registry | 9.0¹ |
| `google-cloud-functions-base` | GCP Cloud Functions (Gen 2) | 8.14 |
| `google-cloud-run-base` | GCP Cloud Run Services and Jobs | 8.14 |
| `google-cloud-storage-base` | GCP Cloud Storage | 8.14 |
| `google-cloud-secret-manager-base` | GCP Secret Manager | 8.14 |
| `google-cloud-pubsub-base` | GCP Pub/Sub | 8.14 |
| `hashicorp-vault-base` | HashiCorp Vault KV and dynamic credentials | 8.14 |
| `google-cloud-kms-base` | GCP Cloud KMS | 8.14 |
| `azure-app-configuration-base` | Read and write Azure App Configuration key-values, feature flags, and snapshots | 8.14 |
| `azure-blob-storage-base` | Azure Blob Storage | 8.14 |
| `azure-container-apps-base` | Container Apps environments, apps, revisions, and jobs (create/update/delete, revision activation, job execution start/stop) | 8.14 |
| `azure-container-registry-base` | Azure Container Registry | 8.14 |
| `azure-functions-base` | Deploy, invoke, and configure Azure Function Apps (zip deploy, run-from-package, HTTP invocation, app settings) | 8.14 |
| `azure-key-vault-base` | Azure Key Vault | 8.14 |
| `azure-managed-identity-base` | Azure Managed Identity / IMDS | 8.14 |
| `azure-service-bus-base` | Azure Service Bus | 8.14 |
| `artifactory-base` | JFrog Artifactory | 8.14 |
| `nexus-base` | Sonatype Nexus Repository Manager 3 | 8.14 |
| `bitbucket-cloud-base` | Bitbucket Cloud REST API | 8.14 |
| `bitbucket-data-center-base` | Bitbucket Data Center REST API | 8.14 |
| `gitea-base` | Gitea/Forgejo REST API | 8.14 |

#### Utility extensions

**`gradle-extensions`** is the foundation for writing Gradle plugins in this suite. Add it as
a dependency when building plugins or custom build logic:

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions")
}
```

| Library | Description | Minimum Gradle |
|---------|-------------|--------------|
| `aws-java-extensions` | Config-cache-safe BuildService base class, `AwsBuildServiceParams`, and credential adapters for the AWS SDK for Java | 8.14 |
| `aws-kotlin-extensions` | Base client info interface and credential extensions for the AWS SDK for Kotlin | 9.0¹ |
| `google-cloud-extensions` | Config-cache-safe BuildService base class and `GcpBuildServiceParams` for the Google Cloud SDK | 8.14 |
| `hashicorp-vault-extensions` | Config-cache-safe BuildService base for HashiCorp Vault with token renewal and lease tracking | 8.14 |
| `azure-extensions` | Config-cache-safe BuildService base class and `AzureBuildServiceParams` for the Azure SDK | 8.14 |
| `moshi-extensions` | `ValueSource` implementations and `Provider` extensions for Moshi JSON parsing | 8.14 |
| `pkl-extensions` | `ValueSource` implementations for evaluating Pkl configuration files | 8.14 |
| `snakeyaml-extensions` | `ValueSource` implementation and typed `Provider` extensions for YAML parsing | 8.14 |
| `xml-extensions` | XPath query engine, `ValueSource` implementations, and migration helpers for XML | 8.14 |

## Minimum Gradle

Every component publishes its minimum Gradle version as `org.gradle.plugin.api-version`, so an older Gradle
rejects it at dependency resolution with a clear message. There are two tiers: `8.14` and `9.0`.

¹ The 9.0 tier (the AWS SDK for Kotlin components and `google-cloud-artifact-registry-base`) supports build scripts
from Gradle 9.0. Kotlin-source build logic, such as `kotlin-dsl` convention plugins, needs Gradle 9.4, because the
AWS SDK ships Kotlin 2.4 metadata that the Kotlin 2.2 compiler in Gradle 9.0 to 9.3 cannot read. That minimum follows
the Kotlin version the AWS SDK is built with, so SDK upgrades can raise it.

## Installation

Components are published to GitHub Packages. Add the repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/kelvSYC/gradle-tools")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR")).get()
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN")).get()
            }
        }
    }
}
```

A published BOM and version catalog are available from the `aggregation/` build for coordinating
dependency versions across multiple components.

## Building from source

```bash
./gradlew :build          # Build all components
./gradlew :test           # Run tests
./gradlew :detekt         # Lint
```

Single component:

```bash
./gradlew :<component>:build
```

Requires JDK 21+ (the Gradle daemon is pinned to JDK 21).

## License

See [LICENSE](LICENSE) for details.
