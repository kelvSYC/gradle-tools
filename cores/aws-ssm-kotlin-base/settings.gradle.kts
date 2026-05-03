pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../aws-kotlin-extensions")
includeBuild("../clients-base")
