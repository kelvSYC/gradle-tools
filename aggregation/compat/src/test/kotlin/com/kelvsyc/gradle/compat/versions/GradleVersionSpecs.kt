package com.kelvsyc.gradle.compat.versions

import com.kelvsyc.gradle.compat.TierCompatibilitySpec
import org.gradle.util.GradleVersion

// One subclass per Gradle version. Gradle hands test classes to its test JVMs in turn, in the order it finds them, so
// these are the only classes in this package: found one after another, each goes to a JVM of its own.

// The oldest Gradle of the 8.x tier, which the tier's cores must still resolve and run on.
class Gradle814CompatibilitySpec : TierCompatibilitySpec() {
    init {
        val version = "8.14"
        nexusBaseResolves(version)
        gradleExtensionsResolvesThroughAwsStsJavaBase(version, expectGradle99Variant = false)
        nexusBaseRuns(version)
        awsStsKotlinBaseIsRejectedAtResolution(version)
    }
}

// The oldest Gradle of the 9.0 tier, whose build scripts can use the cores.
class Gradle900CompatibilitySpec : TierCompatibilitySpec() {
    init {
        val version = "9.0.0"
        awsStsKotlinBaseBuildScriptResolves(version)
        awsStsKotlinBaseRuns(version)
    }
}

// The AWS SDK ships Kotlin 2.4 metadata, which only the Kotlin 2.3 compiler of Gradle 9.4 and later can read.
// Kotlin-source build-logic therefore needs Gradle 9.4, while build scripts work from the tier's 9.0.
class Gradle941CompatibilitySpec : TierCompatibilitySpec() {
    init {
        awsStsKotlinBaseBuildLogicResolves("9.4.1")
    }
}

// The Gradle running the build, on which every kind of consumer must work.
class CurrentGradleCompatibilitySpec : TierCompatibilitySpec() {
    init {
        val version = GradleVersion.current().version
        nexusBaseResolves(version)
        awsStsKotlinBaseBuildLogicResolves(version)
        awsStsKotlinBaseBuildScriptResolves(version)
        gradleExtensionsResolvesThroughAwsStsJavaBase(version, expectGradle99Variant = true)
        nexusBaseRuns(version)
        awsStsKotlinBaseRuns(version)
    }
}
