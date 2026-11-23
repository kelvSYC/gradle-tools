group = "com.kelvsyc"

val components = file("cores").listFiles { dir -> dir.isDirectory && dir.resolve("settings.gradle.kts").exists() }
    ?.map { it.name }.orEmpty().sorted()

// The cores with integration tests (TestKit probes of configuration-cache and WorkerExecutor serialization boundaries),
// which root `shardIntegrationTest` runs. When adding a new *-base component with a BuildService, also add it here and
// to the integrationTestComponents set in aggregation/testing/build.gradle.kts.
val integrationTestComponents = listOf(
    "artifactory-base",
    "aws-imds-java-base",
    "aws-s3-java-base",
    "aws-secrets-manager-java-base",
    "aws-sns-java-base",
    "aws-sns-kotlin-base",
    "azure-blob-storage-base",
    "bitbucket-cloud-base",
    "bitbucket-data-center-base",
    "google-cloud-storage-base",
)

// The units that shards are made of, besides one per core.
val compatUnit = "aggregation/compat"
val integrationTestUnit = "integrationTest"

/**
 * The weight of each of `units`, from `weights`, or the median of the cores' weights for a unit missing from it.
 */
fun unitWeights(units: List<String>, weights: Map<String, Double>): Map<String, Double> {
    val coreWeights = components.mapNotNull { weights[it] }.sorted()
    val median = when {
        coreWeights.isEmpty() -> 1.0
        coreWeights.size % 2 == 1 -> coreWeights[coreWeights.size / 2]
        else -> (coreWeights[coreWeights.size / 2 - 1] + coreWeights[coreWeights.size / 2]) / 2
    }
    return units.associateWith { weights[it] ?: median }
}

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
 * [assignShards] assigns to shards with the weights in `gradle/shard-weights.properties` ([unitWeights]). The weights
 * are the relative costs of the units, measured from CI test results by `gradle/shard-weights.py`.
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
    val weightsFile = layout.projectDirectory.file("gradle/shard-weights.properties")
    val properties = java.util.Properties().apply { load(providers.fileContents(weightsFile).asText.get().reader()) }
    val weights = properties.stringPropertyNames().associateWith { name ->
        val weight = properties.getProperty(name)
        weight.toDoubleOrNull()
            ?: throw InvalidUserDataException("gradle/shard-weights.properties: $name must be a number, but is '$weight'.")
    }
    val units = components + compatUnit + if (integration) listOf(integrationTestUnit) else emptyList()
    CheckShard(index, count, unitWeights(units, weights))
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
tasks.register("mergeShardReports") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:mergeShardTestReports"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:collectIntegrationTestReports"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:mergeShardCoverageReport"))
}

tasks.register("jacoco") {
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
}
