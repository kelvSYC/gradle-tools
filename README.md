# gradle-tools

A composite build of Gradle plugins and Kotlin libraries for cloud service integration, build
tooling, and plugin development. Published to GitHub Packages under the group `com.kelvsyc.gradle`.

## Component types

Every published component lives under `cores/` and falls into one of three categories:

### Cores

Standalone Gradle plugins that solve a specific build problem. Apply them directly in your
`build.gradle.kts`:

| Component | Plugin ID | Description |
|-----------|-----------|-------------|
| `dokka-dash` | `com.kelvsyc.gradle.dokka-dash` | Packages Dokka HTML output into Dash docset bundles |
| `git-core` | `com.kelvsyc.gradle.git-core` | Git archival tasks and JGit extensions for remote repositories |
| `jfrog-cli-core` | `com.kelvsyc.gradle.jfrog-cli-core` | Gradle tasks wrapping the JFrog CLI |

### Bases

Bases provide managed SDK client infrastructure for a specific cloud service. Each ships an
`AbstractClientBuildService` subclass (or a Gradle plugin that registers one) so that downstream
plugins and tasks can access pre-configured SDK clients without managing their lifecycle. Bases
that have completed the migration to `AbstractClientBuildService` are published as Kotlin
libraries; bases still on the legacy `ClientsBaseService` registry are published as Gradle plugins.

**AWS** bases come in **Java SDK** and **Kotlin SDK** variants with mirrored APIs, since
the two AWS SDKs are distinct libraries:

| AWS Service | Java | Kotlin |
|-------------|------|--------|
| S3 | `aws-s3-java-base` (library) | `aws-s3-kotlin-base` (plugin) |
| SQS | `aws-sqs-java-base` (plugin) | `aws-sqs-kotlin-base` (library) |
| SNS | `aws-sns-java-base` (library) | `aws-sns-kotlin-base` (plugin) |
| SES | `aws-ses-java-base` (plugin) | `aws-ses-kotlin-base` (plugin) |
| Secrets Manager | `aws-secrets-manager-java-base` (library) | `aws-secrets-manager-kotlin-base` (library) |
| CodeArtifact | `aws-codeartifact-java-base` (plugin) | `aws-codeartifact-kotlin-base` (plugin) |
| ECR | `aws-ecr-java-base` (plugin) | `aws-ecr-kotlin-base` (library) |
| IMDS | `aws-imds-java-base` (plugin) | `aws-imds-kotlin-base` (plugin) |
| KMS | `aws-kms-java-base` (library) | `aws-kms-kotlin-base` (library) |
| Lambda | `aws-lambda-java-base` (plugin) | `aws-lambda-kotlin-base` (plugin) |
| SSM Parameter Store | `aws-ssm-java-base` (library) | `aws-ssm-kotlin-base` (library) |
| STS | `aws-sts-java-base` (library) | `aws-sts-kotlin-base` (library) |

**Other** bases:

| Component | Description | Form |
|-----------|-------------|------|
| `google-cloud-artifact-registry-base` | GCP Artifact Registry | plugin |
| `google-cloud-storage-base` | GCP Cloud Storage | plugin |
| `google-cloud-secret-manager-base` | GCP Secret Manager | plugin |
| `google-cloud-pubsub-base` | GCP Pub/Sub | plugin |
| `azure-blob-storage-base` | Azure Blob Storage | library |
| `azure-key-vault-base` | Azure Key Vault | plugin |
| `artifactory-base` | JFrog Artifactory | plugin |
| `bitbucket-cloud-base` | Bitbucket Cloud REST API | plugin |
| `bitbucket-data-center-base` | Bitbucket Data Center REST API | plugin |

All plugin IDs are prefixed with `com.kelvsyc.gradle.`.

The shared client registry itself is provided by **`clients-base`**
(`com.kelvsyc.gradle.clients-base`). End users typically do not apply this plugin directly;
it is pulled in as a dependency by the service-specific bases.

### Extensions

Kotlin libraries with no plugin code. Add them as regular dependencies when writing your own
Gradle plugins or build logic:

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions")
}
```

| Library | Description |
|---------|-------------|
| `gradle-extensions` | Kotlin DSL extensions and utility types for Gradle plugin development |
| `aws-java-extensions` | Base client info interface and credential adapters for the AWS SDK for Java |
| `aws-kotlin-extensions` | Base client info interface and credential extensions for the AWS SDK for Kotlin |
| `moshi-extensions` | `ValueSource` implementations and `Provider` extensions for Moshi JSON parsing |
| `pkl-extensions` | `ValueSource` implementations for evaluating Pkl configuration files |
| `xml-extensions` | XPath query engine, `ValueSource` implementations, and migration helpers for XML |

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
