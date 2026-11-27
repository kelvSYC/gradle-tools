group = "com.kelvsyc"

val components = file("cores").listFiles { dir -> dir.isDirectory && dir.resolve("settings.gradle.kts").exists() }
    ?.map { it.name }.orEmpty().sorted()

// The cores with integration tests (TestKit probes of configuration-cache and WorkerExecutor serialization boundaries),
// which root `shardIntegrationTest` runs: those with an `integrationTest` source directory. Applying the
// `gradle-integration-test` convention without sources, or having sources without applying it, fails the build.
val integrationConvention = "com.kelvsyc.internal.gradle-tools.gradle-integration-test"
val integrationTestComponents = components.filter { core ->
    val dir = file("cores/$core")
    val hasSources = dir.resolve("src/integrationTest").isDirectory
    val appliesConvention = dir.resolve("build.gradle.kts").readText().contains(integrationConvention)
    if (appliesConvention && !hasSources) {
        throw InvalidUserDataException(
            "Core '$core' applies $integrationConvention but has no src/integrationTest sources; add them or remove the plugin."
        )
    }
    if (hasSources && !appliesConvention) {
        throw InvalidUserDataException(
            "Core '$core' has src/integrationTest sources but does not apply $integrationConvention."
        )
    }
    hasSources
}

// The units that shards are made of, besides one per core.
val compatUnit = "aggregation/compat"
val integrationTestUnit = "integrationTest"

// The cost of the heavy units in light-core equivalents (every other core counts 1), estimated from CI test times
// (heavy unit's seconds / median light core's); revisit when a shard finishes minutes after the others.
val heavyUnitWeights = mapOf(
    "gradle-extensions" to 27.0,
    integrationTestUnit to 14.0,
    compatUnit to 12.0,
    "dokka-dash" to 7.0
)

/**
 * Assigns each unit of `weights` to one of `count` shards (1-based) by greedy longest-processing-time: heaviest unit
 * first (ties by name), each to the shard with the smallest weight so far (ties to the lowest index).
 */
fun assignShards(weights: Map<String, Double>, count: Int): Map<String, Int> {
    val sums = DoubleArray(count)
    return weights.entries
        .sortedWith(compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key })
        .associate { (unit, weight) ->
            val shard = sums.indices.minWith(compareBy<Int> { sums[it] }.thenBy { it })
            sums[shard] += weight
            unit to shard + 1
        }
}

/**
 * Shard `index` of `count` (1-based) of the root `check`, from `-Pcheck.shard=<index>/<count>`.
 *
 * Every core, `aggregation/compat` and, with `-Pcheck.shard.integration=true`, the integration tests are units, which
 * [assignShards] assigns to shards with the weights of [heavyUnitWeights] (1 for every other unit).
 */
class CheckShard(val index: Int, val count: Int, val weights: Map<String, Double>) {
    val assignment = assignShards(weights, count)

    fun owns(unit: String): Boolean = assignment[unit] == index
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
    val integration = providers.gradleProperty("check.shard.integration").orNull?.let {
        it.toBooleanStrictOrNull()
            ?: throw InvalidUserDataException("check.shard.integration must be true or false, but is '$it'.")
    } ?: false
    val units = components + compatUnit + if (integration) listOf(integrationTestUnit) else emptyList()
    CheckShard(index, count, units.associateWith { heavyUnitWeights[it] ?: 1.0 })
}.orNull

// The cores the root `check` covers.
val checkComponents = checkShard?.let { shard -> components.filter(shard::owns) } ?: components

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

listOf("checkKotlinAbi", "updateKotlinAbi").forEach { abiTask ->
    tasks.register(abiTask) {
        components.forEach {
            dependsOn(gradle.includedBuild(it).task(":$abiTask"))
        }
    }
}

// The dependency license gate of every component (licensee, see com.kelvsyc.internal.gradle-tools.license-check).
tasks.register("checkLicense") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":licensee"))
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
        if (checkShard.owns(compatUnit)) {
            dependsOn(gradle.includedBuild("aggregation").task(":compat:test"))
        }
    }
    checkComponents.forEach {
        dependsOn(gradle.includedBuild(it).task(":verifyGradleLinkage"))
        dependsOn(gradle.includedBuild(it).task(":checkKotlinAbi"))
        dependsOn(gradle.includedBuild(it).task(":licensee"))
        dependsOn(gradle.includedBuild(it).task(":verifyPomDependencyVersions"))
    }
}

// The integration tests, of every core with them, or with -Pcheck.shard of the shard that owns the integration-test
// unit (none on the others). The shard's assignment counts that unit only with -Pcheck.shard.integration=true, so a
// sharded invocation of this task without it fails rather than run no integration tests on any shard. CI runs it in
// the same invocation as the shard's `check`.
tasks.register("shardIntegrationTest") {
    description = "Runs the integration tests, or with -Pcheck.shard those of the shard that owns them."
    group = "verification"
    val unassigned = checkShard != null && integrationTestUnit !in checkShard.assignment
    if (checkShard == null || checkShard.owns(integrationTestUnit)) {
        integrationTestComponents.forEach {
            dependsOn(gradle.includedBuild(it).task(":integrationTest"))
        }
    }
    doLast {
        if (unassigned) {
            throw InvalidUserDataException("With check.shard, shardIntegrationTest needs -Pcheck.shard.integration=true.")
        }
    }
}

// Prints the assignment of every shard of -Pcheck.shard's count: each shard's units and weight sum.
tasks.register("shardAssignment") {
    description = "Prints every shard's units and weight sum for -Pcheck.shard's shard count."
    group = "help"
    val summary = checkShard?.let { shard ->
        (1..shard.count).joinToString("\n") { index ->
            val units = shard.assignment.filterValues { it == index }.keys.sortedByDescending { shard.weights[it] }
            val sum = units.sumOf { shard.weights.getValue(it) }
            "Shard $index/${shard.count}: weight ${sum.toLong()}, ${units.size} units: ${units.joinToString()}"
        }
    }
    doLast {
        println(summary ?: throw InvalidUserDataException("shardAssignment needs -Pcheck.shard=<index>/<count>."))
    }
}

// Builds the aggregate test, coverage and integration-test reports from the results of earlier sharded `check` runs
// that are already on disk (CI copies every shard's results into place first). Runs no tests.
tasks.register("mergeShardTestReports") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:mergeShardTestReports"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:collectIntegrationTestReports"))
}

// The test reports and the coverage report. CI runs only mergeShardTestReports on pull requests.
tasks.register("mergeShardReports") {
    dependsOn("mergeShardTestReports")
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:mergeShardCoverageReport"))
}

tasks.register("jacoco") {
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
}
