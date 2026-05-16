pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../clients-base")
includeBuild("../hashicorp-vault-extensions")
