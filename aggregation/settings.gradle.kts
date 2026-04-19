import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension

pluginManagement {
    includeBuild("../gradle/plugins")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.ajoberstar.reckon.settings") version "1.0.0"
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
include("dokka")
include("jacoco")
include("bom")
include("testing")

// Builds to be aggregated
file("../cores").list { dir, _ -> dir.isDirectory  }?.forEach {
    includeBuild("../cores/$it")
}
file("../extensions").list { dir, _ -> dir.isDirectory  }?.forEach {
    includeBuild("../extensions/$it")
}
