import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("AWS Lambda JVM Plugin")
}

gradlePlugin {
    plugins.register("aws-lambda-jvm") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmPlugin"
    }
    plugins.register("aws-lambda-jvm-runtime") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.runtime"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmRuntimePlugin"
    }
    plugins.register("aws-lambda-jvm-fat-package") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.fat-package"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmFatPackagePlugin"
    }
    plugins.register("aws-lambda-jvm-thin-package") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.thin-package"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmThinPackagePlugin"
    }
    plugins.register("aws-lambda-jvm-layered") {
        id = "com.kelvsyc.gradle.aws-lambda-jvm.layered"
        implementationClass = "com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmLayeredPlugin"
    }
}

dependencies {
    testImplementation(libs.mockk)
}
