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

Gradle plugins that provide managed SDK client infrastructure for a specific cloud service.
These are building blocks for plugin developers: each registers a typed client configuration
into the shared `clients-base` registry so that downstream plugins and tasks can access
pre-configured SDK clients without managing their lifecycle.

**AWS** bases come in **Java SDK** and **Kotlin SDK** variants with mirrored APIs, since
the two AWS SDKs are distinct libraries:

| AWS Service | Java plugin | Kotlin plugin |
|-------------|-------------|---------------|
| S3 | `aws-s3-java-base` | `aws-s3-kotlin-base` |
| SQS | `aws-sqs-java-base` | `aws-sqs-kotlin-base` |
| SNS | `aws-sns-java-base` | `aws-sns-kotlin-base` |
| SES | `aws-ses-java-base` | `aws-ses-kotlin-base` |
| Secrets Manager | `aws-secrets-manager-java-base` | `aws-secrets-manager-kotlin-base` |
| CodeArtifact | `aws-codeartifact-java-base` | `aws-codeartifact-kotlin-base` |
| ECR | `aws-ecr-java-base` | `aws-ecr-kotlin-base` |
| IMDS | `aws-imds-java-base` | `aws-imds-kotlin-base` |
| KMS | `aws-kms-java-base` | `aws-kms-kotlin-base` |
| Lambda | `aws-lambda-java-base` | `aws-lambda-kotlin-base` |
| SSM Parameter Store | `aws-ssm-java-base` | `aws-ssm-kotlin-base` |
| STS | `aws-sts-java-base` | `aws-sts-kotlin-base` |

**Other** bases have a single plugin each:

| Component | Description |
|-----------|-------------|
| `google-cloud-artifact-registry-base` | GCP Artifact Registry |
| `google-cloud-storage-base` | GCP Cloud Storage |
| `google-cloud-secret-manager-base` | GCP Secret Manager |
| `google-cloud-pubsub-base` | GCP Pub/Sub |
| `azure-blob-storage-base` | Azure Blob Storage |
| `artifactory-base` | JFrog Artifactory |
| `bitbucket-cloud-base` | Bitbucket Cloud REST API |
| `bitbucket-data-center-base` | Bitbucket Data Center REST API |

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
