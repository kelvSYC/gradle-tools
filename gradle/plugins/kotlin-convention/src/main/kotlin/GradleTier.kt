import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * The oldest Gradle version a core supports, and what its published code is compiled against to keep that promise.
 *
 * A core declares its tier with the [PROPERTY] Gradle property in its `gradle.properties`.
 *
 * @property minimum The value of [PROPERTY] that selects this tier.
 * @property publicApiVersion The version of `org.gradle.experimental:gradle-public-api` that `main` compiles against.
 * @property embeddedKotlin The Kotlin version embedded in the [minimum] Gradle, used for `kotlin-stdlib` and
 *   `kotlin-reflect` on `main`'s compile classpath.
 * @property kotlinVersion The Kotlin `apiVersion` and `languageVersion` for `main`, matching [embeddedKotlin].
 */
enum class GradleTier(
    val minimum: String,
    val publicApiVersion: String,
    val embeddedKotlin: String,
    val kotlinVersion: KotlinVersion,
) {
    GRADLE_8_14("8.14", "8.14", "2.0.21", KotlinVersion.KOTLIN_2_0),
    GRADLE_9_0("9.0", "9.0.0", "2.2.0", KotlinVersion.KOTLIN_2_2),
    ;

    companion object {
        /** The Gradle property that declares a core's tier. Absent means [GRADLE_8_14]. */
        const val PROPERTY = "com.kelvsyc.gradle.minimumGradle"
    }
}

/**
 * Returns the tier this project declares with [GradleTier.PROPERTY], defaulting to [GradleTier.GRADLE_8_14].
 */
fun Project.gradleTier(): GradleTier {
    val value = providers.gradleProperty(GradleTier.PROPERTY).orNull ?: return GradleTier.GRADLE_8_14
    return GradleTier.entries.find { it.minimum == value }
        ?: error(
            "$value is not a supported value for ${GradleTier.PROPERTY}; " +
                "use one of ${GradleTier.entries.joinToString { it.minimum }}"
        )
}

/**
 * Configures a dependency on `org.gradle.experimental:gradle-public-api` so that it resolves.
 */
fun ExternalModuleDependency.gradlePublicApi() {
    // The artifact only publishes this capability.
    capabilities { requireCapability("org.gradle.experimental:gradle-public-api-internal") }
    // The API pins jspecify strictly, which clashes with newer versions elsewhere on the classpath. Consumers declare
    // jspecify themselves, so the API's nullness annotations stay visible to the compiler.
    exclude(group = "org.jspecify")
}
