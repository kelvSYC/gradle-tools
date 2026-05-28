# aws-lambda-jvm-plugin

Composable Gradle plugins for packaging JVM Lambda functions into deployable ZIP artifacts.

## Plugin IDs

| Plugin ID | Type | Description |
|-----------|------|-------------|
| `com.kelvsyc.gradle.aws-lambda-jvm` | umbrella | Adds `aws-lambda-java-core` to `implementation` and registers fat-JAR packaging tasks |
| `com.kelvsyc.gradle.aws-lambda-jvm.layered` | umbrella | Adds `aws-lambda-java-core` to `implementation` and registers thin-JAR packaging tasks |
| `com.kelvsyc.gradle.aws-lambda-jvm.runtime` | atomic | Adds `aws-lambda-java-core` to `implementation` only |
| `com.kelvsyc.gradle.aws-lambda-jvm.fat-package` | atomic | Registers fat-JAR packaging tasks only |
| `com.kelvsyc.gradle.aws-lambda-jvm.thin-package` | atomic | Registers thin-JAR packaging tasks only |

## Usage

Apply the umbrella plugin alongside a JVM language plugin:

```kotlin
plugins {
    kotlin("jvm")
    id("com.kelvsyc.gradle.aws-lambda-jvm") version "<version>"
}
```

This registers two tasks:
- `lambdaFatJar` — all-in-one JAR with all runtime dependencies merged in.
- `lambdaDeploymentZip` — wraps the fat JAR in a ZIP ready for Lambda upload.

Run `./gradlew lambdaDeploymentZip` to produce the artifact.

### Layered deployment

For functions backed by a Lambda layer that provides the runtime dependencies:

```kotlin
plugins {
    kotlin("jvm")
    id("com.kelvsyc.gradle.aws-lambda-jvm.layered") version "<version>"
}
```

This registers:
- `lambdaJar` — JAR containing only the function's compiled classes.
- `lambdaDeploymentZip` — ZIP with the thin JAR at the root and dependencies under `lib/`.

### Extension

Both packaging plugins expose the `awsLambdaJvm` extension:

```kotlin
awsLambdaJvm {
    archiveBaseName.set("my-function")
}
```

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `archiveBaseName` | `Property<String>` | project name | Base name for generated archives |
| `deploymentZipFile` | `RegularFileProperty` | set by packaging plugin | Path to the deployment ZIP (read-only for consumers) |

## Upload

To also wire the deployment ZIP to an AWS Lambda upload task, add the
`aws-lambda-jvm-deploy-plugin` component and apply `com.kelvsyc.gradle.aws-lambda-jvm.deploy`
(fat-JAR upload) or `com.kelvsyc.gradle.aws-lambda-jvm.layered-deploy` (thin-JAR upload).
