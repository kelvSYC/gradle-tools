# aws-lambda-jvm-deploy-plugin

Gradle plugins that wire the deployment ZIP produced by `aws-lambda-jvm-plugin` to an AWS Lambda
upload task. Depends on `aws-lambda-java-base` for the upload task implementation.

## Plugin IDs

| Plugin ID | Type | Description |
|-----------|------|-------------|
| `com.kelvsyc.gradle.aws-lambda-jvm.deploy` | umbrella | Fat-JAR packaging + Lambda upload wired end-to-end |
| `com.kelvsyc.gradle.aws-lambda-jvm.layered-deploy` | umbrella | Thin-JAR packaging + Lambda upload wired end-to-end |
| `com.kelvsyc.gradle.aws-lambda-jvm.upload` | atomic | Upload task only, for use alongside an explicit packaging plugin |

## Usage

### All-in-one (fat JAR + upload)

```kotlin
plugins {
    kotlin("jvm")
    id("com.kelvsyc.gradle.aws-lambda-jvm.deploy") version "<version>"
}

awsLambdaJvmDeploy {
    functionName.set("my-lambda-function")
    publish.set(true)           // optional: publish a new version after upload
    regionId.set("us-east-1")  // optional: defaults to SDK region provider chain
}
```

Run `./gradlew uploadLambdaFunction` to package and upload the function.

### Layered deployment (thin JAR + upload)

```kotlin
plugins {
    kotlin("jvm")
    id("com.kelvsyc.gradle.aws-lambda-jvm.layered-deploy") version "<version>"
}

awsLambdaJvmDeploy {
    functionName.set("my-lambda-function")
}
```

### Atomic composition

Apply the packaging and upload plugins separately when you need to customize the pipeline:

```kotlin
plugins {
    kotlin("jvm")
    id("com.kelvsyc.gradle.aws-lambda-jvm.fat-package") version "<version>"
    id("com.kelvsyc.gradle.aws-lambda-jvm.upload") version "<version>"
}
```

Plugin application order does not matter — the upload task's ZIP input is wired lazily.

## Extensions

### `awsLambdaJvmDeploy`

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `functionName` | `Property<String>` | Yes | Function name, partial ARN, or full ARN |
| `publish` | `Property<Boolean>` | No | Publish a new version after upload; defaults to `false` |
| `regionId` | `Property<String>` | No | AWS region; defaults to SDK provider chain |

## Credentials

The upload task uses the AWS SDK's default credential chain:
`AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` env vars → `~/.aws/credentials` → IAM role.
No credential configuration is required in most CI/CD environments.
