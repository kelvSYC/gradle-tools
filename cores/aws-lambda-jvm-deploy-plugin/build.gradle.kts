import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("AWS Lambda JVM Deploy Plugin")
}

gradlePlugin {
    plugins.register("aws-lambda-jvm-upload") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.upload"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.deploy.AwsLambdaJvmUploadPlugin"
    }
    plugins.register("aws-lambda-jvm-deploy") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.deploy"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.deploy.AwsLambdaJvmDeployPlugin"
    }
    plugins.register("aws-lambda-jvm-layered-deploy") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.layered-deploy"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.deploy.AwsLambdaJvmLayeredDeployPlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-lambda-jvm-plugin")
    api("com.kelvsyc.gradle:aws-lambda-java-base")
    testImplementation(libs.mockk)
}
