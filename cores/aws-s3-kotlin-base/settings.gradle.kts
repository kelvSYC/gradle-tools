pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../clients-base")
includeBuild("../aws-kotlin-extensions")
includeBuild("../gradle-extensions")
