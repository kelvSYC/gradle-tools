import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Ant Bridge")
}

gradlePlugin {
    plugins.register("ant-bridge") {
        id = "com.kelvsyc.gradle.ant-bridge"
        implementationClass = "com.kelvsyc.gradle.plugins.AntBridgePlugin"
    }
}

val antShim = sourceSets.create("antShim")

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    "antShimCompileOnly"(local.ant)
    "antShimCompileOnly"(local.ant.junit)

    testImplementation(gradleTestKit())
    testImplementation(libs.mockk)
}

// Shim classes ship as resources under a non-class name: the plugin's loader never sees them as classes and
// verifyGradleLinkage does not scan their Ant references.
tasks.processResources {
    from(antShim.output.classesDirs) {
        into("ant-bridge-shim")
        rename { "$it.bin" }
    }
}

val antTestClasspath = configurations.resolvable("antTestClasspath") {
    isTransitive = true
}
val antContribTestClasspath = configurations.resolvable("antContribTestClasspath") {
    isTransitive = true
}
val antTestDeps = configurations.dependencyScope("antTestDeps")
val antContribTestDeps = configurations.dependencyScope("antContribTestDeps")
antTestClasspath.configure { extendsFrom(antTestDeps.get()) }
antContribTestClasspath.configure {
    extendsFrom(antContribTestDeps.get())
    exclude(group = "ant")
}
dependencies {
    antTestDeps(local.ant)
    antTestDeps(local.ant.junit)
    antContribTestDeps(local.ant.contrib)
}

tasks.test {
    // One AntBridgeGradleVersionSpec subclass per Gradle version (added later), each in its own test JVM.
    maxParallelForks = 2
    val antClasspath = files(antTestClasspath)
    val contribClasspath = files(antContribTestClasspath)
    val catalogAntVersion = local.versions.ant
    val testKitDir = layout.buildDirectory.dir("gradle-version-testkit")
    inputs.files(antClasspath).withNormalizer(ClasspathNormalizer::class.java)
    inputs.files(contribClasspath).withNormalizer(ClasspathNormalizer::class.java)
    inputs.property("catalogAntVersion", catalogAntVersion)
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-DantBridge.test.antClasspath=${antClasspath.files.joinToString(File.pathSeparator)}",
            "-DantBridge.test.contribClasspath=${contribClasspath.files.joinToString(File.pathSeparator)}",
            "-DantBridge.test.catalogAntVersion=${catalogAntVersion.get()}",
            "-DgradleVersionTest.testKitDir=${testKitDir.get().asFile.path}",
        )
    })
}
