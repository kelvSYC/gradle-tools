pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../aws-lambda-jvm-plugin")
includeBuild("../aws-lambda-java-base")
includeBuild("../clients-base")
