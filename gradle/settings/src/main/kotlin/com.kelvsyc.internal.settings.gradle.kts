import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension

// Settings plugin to be applied to all builds located in the cores directory
pluginManagement {
    includeBuild("../../gradle/plugins")
}

plugins {
    id("org.ajoberstar.reckon.settings")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    includeBuild("../../gradle/platform")

    versionCatalogs.register("libs") {
        from(files("../../gradle/libs.versions.toml"))
    }
}

configure<ReckonExtension> {
    setDefaultInferredScope(Scope.PATCH)
    stages("beta", "rc", "final")
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
}
