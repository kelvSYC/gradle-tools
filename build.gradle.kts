group = "com.kelvsyc"

val components = file("cores").listFiles { dir -> dir.isDirectory && dir.resolve("settings.gradle.kts").exists() }
    ?.map { it.name }.orEmpty().sorted()

/**
 * Shard `index` of `count` (1-based) of the root `check`, from `-Pcheck.shard=<index>/<count>`, covering the compat tests on
 * shard 1 and a share of the cores.
 *
 * The sorted `components` are dealt out back and forth: shards 1 to `count`, then `count` to 1, and so on. Plain
 * round-robin would put the `-java-` and `-kotlin-` variant of each AWS service, adjacent in the sorted list, on
 * alternate shards, so that the heavier Kotlin SDK cores would gather on half of them.
 */
class CheckShard(val index: Int, val count: Int) {
    fun covers(position: Int): Boolean {
        val turn = position % (2 * count)
        return index == if (turn < count) turn + 1 else 2 * count - turn
    }
}

val checkShard = providers.gradleProperty("check.shard").map { value ->
    val parts = value.split("/").map { it.trim().toIntOrNull() }
    val index = parts.getOrNull(0)
    val count = parts.getOrNull(1)
    if (parts.size != 2 || index == null || count == null) {
        throw InvalidUserDataException("check.shard must be <index>/<count>, for example 1/4, but is '$value'.")
    }
    if (count < 1 || index !in 1..count) {
        throw InvalidUserDataException("check.shard must satisfy 1 <= index <= count, but is '$value'.")
    }
    CheckShard(index, count)
}.orNull

// The cores the root `check` covers.
val checkComponents = checkShard?.let { shard -> components.filterIndexed { position, _ -> shard.covers(position) } }
    ?: components

tasks.register("clean") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
}

tasks.register("assemble") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":dokka:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:$name"))
}

tasks.register("build") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":catalog:generateCatalogAsToml"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
}

tasks.register("publish") {
    group = "publishing"
    description = "Publish all components"

    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":catalog:$name"))
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
}

tasks.register("dokkaGenerate") {
    dependsOn(gradle.includedBuild("aggregation").task(":dokka:build"))
}

tasks.register("detekt") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
    dependsOn(gradle.includedBuild("aggregation").task(":compat:test"))
}

tasks.register("check") {
    if (checkShard == null) {
        dependsOn("test")
        dependsOn("detekt")
    } else {
        // The aggregate test report depends on every core's tests, so a shard depends on its cores' tests directly and
        // leaves the reports to be merged from all shards (mergeShardReports).
        checkComponents.forEach {
            dependsOn(gradle.includedBuild(it).task(":test"))
            dependsOn(gradle.includedBuild(it).task(":detekt"))
        }
        if (checkShard.index == 1) {
            dependsOn(gradle.includedBuild("aggregation").task(":compat:test"))
        }
    }
    checkComponents.forEach {
        dependsOn(gradle.includedBuild(it).task(":verifyGradleLinkage"))
        dependsOn(gradle.includedBuild(it).task(":verifyPomDependencyVersions"))
    }
}

// Builds the aggregate test, coverage and integration-test reports from the results of earlier sharded `check` runs
// that are already on disk (CI copies every shard's results into place first). Runs no tests.
tasks.register("mergeShardReports") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:mergeShardTestReports"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:collectIntegrationTestReports"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:mergeShardCoverageReport"))
}

tasks.register("jacoco") {
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
}
