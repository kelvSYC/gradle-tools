import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension

pluginManagement {
    includeBuild("../gradle/plugins")
}

plugins {
    id("org.ajoberstar.reckon.settings") version "0.19.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    includeBuild("../gradle/platform")

    versionCatalogs.register("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}

configure<ReckonExtension> {
    setDefaultInferredScope(Scope.PATCH)
    stages("beta", "rc", "final")
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
}

include("catalog")
include("dokkatoo")
include("jacoco")
include("platform")

// Builds to be aggregated
file("../cores").list { dir, _ -> dir.isDirectory  }?.forEach {
    includeBuild("../cores/$it")
}
file("../extensions").list { dir, _ -> dir.isDirectory  }?.forEach {
    includeBuild("../extensions/$it")
}
