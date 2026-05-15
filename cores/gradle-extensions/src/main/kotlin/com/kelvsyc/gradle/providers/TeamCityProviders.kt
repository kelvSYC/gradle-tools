package com.kelvsyc.gradle.providers

import java.io.File
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to TeamCity.
 *
 * TeamCity exposes build parameters through two channels:
 * - **Environment variables** set directly in the build process (e.g. `TEAMCITY_VERSION`, `BUILD_NUMBER`).
 * - **System properties** written to a properties file whose path is given by the `TEAMCITY_BUILD_PROPERTIES_FILE`
 *   environment variable. These are read via [PropertiesFromFileValueSource].
 *
 * **Configuration cache and sensitive parameters:** TeamCity writes "Password" type build parameters to the
 * system properties file in plaintext — the masking applied in the TeamCity UI and build logs is display-only and
 * does not prevent the value from appearing in the file. Any `system.*` parameter defined as "Password" type in the
 * build configuration will therefore be present in the file read by this class. The properties exposed here are
 * limited to well-known, non-sensitive system properties (build IDs, paths, branch names). However, callers who
 * access additional properties from the same file — or who use [configurationPropertiesFilePath] to read the
 * configuration parameters file — risk caching password-type parameter values in the Gradle configuration cache
 * in plaintext. Read sensitive properties inside a [org.gradle.workers.WorkAction] at task execution time instead.
 *
 * See [TeamCity Build Script Interaction](https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html).
 */
abstract class TeamCityProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {

    // region Environment Variables

    /**
     * Provides the TeamCity server version string. This variable is always set in TeamCity builds and can be used
     * to detect whether the build is running under TeamCity.
     */
    val teamcityVersion = providers.environmentVariable("TEAMCITY_VERSION")

    /**
     * Provides the build number assigned to the build, as configured in the build configuration's General Settings.
     */
    val buildNumber = providers.environmentVariable("BUILD_NUMBER")

    /**
     * Provides the VCS revision of the sources included in the build. For Git, this is the full commit SHA.
     */
    val buildVcsNumber = providers.environmentVariable("BUILD_VCS_NUMBER")

    /**
     * Provides `true` if the build is a personal build. Absent when the `BUILD_IS_PERSONAL` environment variable is
     * not set.
     */
    val isPersonal = providers.environmentVariable("BUILD_IS_PERSONAL")
        .map { it.toBooleanStrictOrNull() }

    /**
     * Provides the name of the TeamCity project associated with the build.
     */
    val projectName = providers.environmentVariable("TEAMCITY_PROJECT_NAME")

    /**
     * Provides the name of the build configuration.
     */
    val buildConfName = providers.environmentVariable("TEAMCITY_BUILDCONF_NAME")

    // endregion

    // region Build Properties File

    /**
     * Provides the path to the build properties file, which contains system properties for the build.
     */
    val buildPropertiesFilePath = providers.environmentVariable("TEAMCITY_BUILD_PROPERTIES_FILE")

    private val buildProperties = buildPropertiesFilePath.flatMap { path ->
        providers.propertiesFile(layout.projectDirectory.file(path))
    }.asMap

    /**
     * Provides the internal numeric build ID, unique across the TeamCity server. Sourced from the
     * `teamcity.build.id` system property.
     */
    val buildId = buildProperties.getting("teamcity.build.id")
        .map { it.toLongOrNull() }

    /**
     * Provides the build configuration ID (e.g. `MyProject_MyBuildConfig`). Sourced from the
     * `teamcity.buildType.id` system property.
     */
    val buildTypeId = buildProperties.getting("teamcity.buildType.id")

    /**
     * Provides the URL of the TeamCity server. Sourced from the `teamcity.serverUrl` system property.
     */
    val serverUrl = buildProperties.getting("teamcity.serverUrl")

    /**
     * Provides the name of the build agent running the build. Sourced from the `teamcity.agent.name` system property.
     */
    val agentName = buildProperties.getting("teamcity.agent.name")

    /**
     * Provides the path to the build agent's home directory. Sourced from the `teamcity.agent.home.dir` system
     * property.
     */
    val agentHomeDirPath = buildProperties.getting("teamcity.agent.home.dir")

    /**
     * Provides the build agent's home directory, as a [org.gradle.api.file.Directory].
     */
    val agentHomeDir = layout.dir(agentHomeDirPath.map { File(it) })

    /**
     * Provides the path to the build agent's tools directory. Sourced from the `teamcity.agent.tools.dir` system
     * property.
     */
    val agentToolsDirPath = buildProperties.getting("teamcity.agent.tools.dir")

    /**
     * Provides the build agent's tools directory, as a [org.gradle.api.file.Directory].
     */
    val agentToolsDir = layout.dir(agentToolsDirPath.map { File(it) })

    /**
     * Provides the path to the checkout directory for the build. Sourced from the `teamcity.build.checkoutDir`
     * system property.
     */
    val checkoutDirPath = buildProperties.getting("teamcity.build.checkoutDir")

    /**
     * Provides the checkout directory for the build, as a [org.gradle.api.file.Directory].
     */
    val checkoutDir = layout.dir(checkoutDirPath.map { File(it) })

    /**
     * Provides the path to the working directory for the build. Sourced from the `teamcity.build.workingDir`
     * system property.
     */
    val workingDirPath = buildProperties.getting("teamcity.build.workingDir")

    /**
     * Provides the working directory for the build, as a [org.gradle.api.file.Directory].
     */
    val workingDir = layout.dir(workingDirPath.map { File(it) })

    /**
     * Provides the path to the temporary directory for the build. Sourced from the `teamcity.build.tempDir`
     * system property.
     */
    val tempDirPath = buildProperties.getting("teamcity.build.tempDir")

    /**
     * Provides the temporary directory for the build, as a [org.gradle.api.file.Directory].
     */
    val tempDir = layout.dir(tempDirPath.map { File(it) })

    /**
     * Provides a map of VCS root external IDs to their corresponding revision strings. Sourced from
     * `build.vcs.number.<VCS_root_ID>` system properties. When only a single VCS root is attached, the map
     * contains one entry. When multiple roots are attached, each root's revision is keyed by its external ID
     * (e.g. `MyProject_HttpsGithubComFooBar`).
     */
    val vcsRootRevisions = buildProperties.map { props ->
        val prefix = "build.vcs.number."
        props.filterKeys { it.startsWith(prefix) }
            .mapKeys { (key, _) -> key.removePrefix(prefix) }
    }

    /**
     * Provides the logical branch name for the build. Sourced from the `teamcity.build.branch` system property.
     */
    val branch = buildProperties.getting("teamcity.build.branch")

    /**
     * Provides `true` if the build runs on the default branch. Sourced from the
     * `teamcity.build.branch.is_default` system property.
     */
    val isDefaultBranch = buildProperties.getting("teamcity.build.branch.is_default")
        .map { it.toBooleanStrictOrNull() }

    /**
     * Provides the path to the configuration parameters file. This file contains TeamCity configuration parameters
     * (those without a `system.` or `env.` prefix). Sourced from the `teamcity.configuration.properties.file`
     * system property.
     *
     * **Warning:** TeamCity configuration parameters of "Password" type are written to this file in plaintext.
     * Reading this file via [PropertiesFromFileValueSource] or [ProviderFactory.propertiesFile][org.gradle.api.provider.ProviderFactory]
     * will serialize those values to the Gradle configuration cache. Use this path only to read non-sensitive
     * configuration parameters, or read the file inside a [org.gradle.workers.WorkAction] at task execution time.
     */
    val configurationPropertiesFilePath = buildProperties.getting("teamcity.configuration.properties.file")

    /**
     * Provides the path to the runner parameters file. Sourced from the `teamcity.runner.properties.file` system
     * property.
     */
    val runnerPropertiesFilePath = buildProperties.getting("teamcity.runner.properties.file")

    /**
     * Provides the path to a file listing recently failed tests. Sourced from the
     * `teamcity.tests.recentlyFailedTests.file` system property.
     */
    val recentlyFailedTestsFilePath = buildProperties.getting("teamcity.tests.recentlyFailedTests.file")

    /**
     * Provides the path to a file listing files changed in the build. Sourced from the
     * `teamcity.build.changedFiles.file` system property.
     */
    val changedFilesFilePath = buildProperties.getting("teamcity.build.changedFiles.file")

    // endregion
}
