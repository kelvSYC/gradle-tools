import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension

pluginManagement {
    includeBuild("../gradle/plugins")
}

plugins {
    id("org.ajoberstar.reckon.settings") version "0.19.1"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    includeBuild("../gradle/platform")
}

configure<ReckonExtension> {
    setDefaultInferredScope(Scope.PATCH)
    stages("beta", "rc", "final")
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
}

include("catalog")
include("platform")

// Builds to be aggregated
includeBuild("../cores/git-core")
includeBuild("../cores/gradle-extensions")
