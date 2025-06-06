plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("SNS Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("aws-sns-java-base") {
        id = "com.kelvsyc.gradle.aws-sns-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SnsJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.sns.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
}
