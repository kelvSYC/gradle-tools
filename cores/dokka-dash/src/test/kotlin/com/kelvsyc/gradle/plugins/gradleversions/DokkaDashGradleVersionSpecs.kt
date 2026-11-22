package com.kelvsyc.gradle.plugins.gradleversions

import com.kelvsyc.gradle.plugins.DokkaDashGradleVersionSpec
import org.gradle.util.GradleVersion

// One subclass per Gradle version. Gradle hands test classes to its test JVMs in turn, in the order it finds them, so
// these are the only classes in this package: found one after another, they go to distinct JVMs as long as there are no
// more versions than test JVMs (maxParallelForks, capped at max-workers).

// The oldest Gradle the plugin supports.
class Gradle814DokkaDashSpec : DokkaDashGradleVersionSpec() {
    init {
        versionTests("8.14")
    }
}

// The first Gradle 9 release.
class Gradle900DokkaDashSpec : DokkaDashGradleVersionSpec() {
    init {
        versionTests("9.0.0")
    }
}

class Gradle982DokkaDashSpec : DokkaDashGradleVersionSpec() {
    init {
        versionTests("9.8.2")
    }
}

// The Gradle running the build, so that a wrapper bump exercises the new version automatically.
class CurrentGradleDokkaDashSpec : DokkaDashGradleVersionSpec() {
    // The versions of the other subclasses.
    private val pinnedVersions = setOf("8.14", "9.0.0", "9.8.2")

    init {
        GradleVersion.current().version.takeUnless { it in pinnedVersions }?.let(::versionTests)
    }
}
