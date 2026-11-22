package com.kelvsyc.gradle.variantselection

import com.kelvsyc.gradle.GradleVariantSelectionSpec
import org.gradle.util.GradleVersion

// One subclass per Gradle version. Gradle hands test classes to its test JVMs in turn, in the order it finds them, so
// these are the only classes in this package: found one after another, each goes to a JVM of its own.

// The oldest Gradle the baseline variant supports.
class Gradle814VariantSelectionSpec : GradleVariantSelectionSpec() {
    init {
        versionTests("8.14")
    }
}

// The first Gradle 9 release.
class Gradle900VariantSelectionSpec : GradleVariantSelectionSpec() {
    init {
        versionTests("9.0.0")
    }
}

// The newest Gradle that still selects the baseline variant.
class Gradle982VariantSelectionSpec : GradleVariantSelectionSpec() {
    init {
        versionTests("9.8.2")
    }
}

// The oldest Gradle that selects the gradle99 variant.
class Gradle990VariantSelectionSpec : GradleVariantSelectionSpec() {
    init {
        val version = "9.9.0"
        versionTests(version)
        configurationCacheDoesNotChangeSelection(version)
        mavenOnlyConsumerGetsTheBaseline(version)
    }
}

// The Gradle running the build, so that a wrapper bump exercises the new version automatically.
class CurrentGradleVariantSelectionSpec : GradleVariantSelectionSpec() {
    // The versions of the other subclasses.
    private val pinnedVersions = setOf("8.14", "9.0.0", "9.8.2", "9.9.0")

    init {
        GradleVersion.current().version.takeUnless { it in pinnedVersions }?.let(::versionTests)
    }
}
