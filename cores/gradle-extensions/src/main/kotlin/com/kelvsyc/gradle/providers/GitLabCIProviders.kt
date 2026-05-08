package com.kelvsyc.gradle.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to GitLab CI/CD.
 *
 * See [GitLab CI/CD Predefined Variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html).
 * Sensitive variables (tokens, passwords, deploy credentials) are intentionally excluded.
 */
@Suppress("TooManyFunctions")
abstract class GitLabCIProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {

    // region General CI

    /**
     * Provides `true` if Gradle is running in a CI/CD job. Defaults to `false` when the `CI` environment variable is
     * absent.
     */
    val ci = providers.environmentVariable("CI")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides `true` if Gradle is running in GitLab CI/CD. Defaults to `false` when the `GITLAB_CI` environment
     * variable is absent.
     */
    val gitlabCI = providers.environmentVariable("GITLAB_CI")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides `"yes"` if the job is executed in a CI/CD environment.
     */
    val ciServer = providers.environmentVariable("CI_SERVER")

    // endregion

    // region Server / API

    /**
     * Provides the GitLab API v4 root URL.
     */
    val apiV4Url = providers.environmentVariable("CI_API_V4_URL")

    /**
     * Provides the GitLab GraphQL API root URL.
     */
    val apiGraphqlUrl = providers.environmentVariable("CI_API_GRAPHQL_URL")

    /**
     * Provides the fully qualified domain name of the GitLab instance (e.g. `gitlab.example.com:8080`).
     */
    val serverFqdn = providers.environmentVariable("CI_SERVER_FQDN")

    /**
     * Provides the host of the GitLab instance URL, without protocol or port.
     */
    val serverHost = providers.environmentVariable("CI_SERVER_HOST")

    /**
     * Provides the name of the CI/CD server coordinating jobs.
     */
    val serverName = providers.environmentVariable("CI_SERVER_NAME")

    /**
     * Provides the port of the GitLab instance URL.
     */
    val serverPort = providers.environmentVariable("CI_SERVER_PORT")
        .map(String::toIntOrNull)

    /**
     * Provides the protocol of the GitLab instance URL (e.g. `https`).
     */
    val serverProtocol = providers.environmentVariable("CI_SERVER_PROTOCOL")

    /**
     * Provides the SSH host of the GitLab instance, used for Git operations over SSH.
     */
    val serverShellSshHost = providers.environmentVariable("CI_SERVER_SHELL_SSH_HOST")

    /**
     * Provides the SSH port of the GitLab instance, used for Git operations over SSH.
     */
    val serverShellSshPort = providers.environmentVariable("CI_SERVER_SHELL_SSH_PORT")
        .map(String::toIntOrNull)

    /**
     * Provides the GitLab revision that schedules jobs.
     */
    val serverRevision = providers.environmentVariable("CI_SERVER_REVISION")

    /**
     * Provides the base URL of the GitLab instance, including protocol and port.
     */
    val serverUrl = providers.environmentVariable("CI_SERVER_URL")

    /**
     * Provides the full version string of the GitLab instance.
     */
    val serverVersion = providers.environmentVariable("CI_SERVER_VERSION")

    /**
     * Provides the major version number of the GitLab instance.
     */
    val serverVersionMajor = providers.environmentVariable("CI_SERVER_VERSION_MAJOR")
        .map(String::toIntOrNull)

    /**
     * Provides the minor version number of the GitLab instance.
     */
    val serverVersionMinor = providers.environmentVariable("CI_SERVER_VERSION_MINOR")
        .map(String::toIntOrNull)

    /**
     * Provides the patch version number of the GitLab instance.
     */
    val serverVersionPatch = providers.environmentVariable("CI_SERVER_VERSION_PATCH")
        .map(String::toIntOrNull)

    // endregion

    // region Commit

    /**
     * Provides the commit author in `Name <email>` format.
     */
    val commitAuthor = providers.environmentVariable("CI_COMMIT_AUTHOR")

    /**
     * Provides the previous latest commit SHA on the branch or tag. Always `0000000000000000000000000000000000000000`
     * for merge request, scheduled, or first pipelines.
     */
    val commitBeforeSha = providers.environmentVariable("CI_COMMIT_BEFORE_SHA")

    /**
     * Provides the commit branch name. Not available in merge request or tag pipelines.
     */
    val commitBranch = providers.environmentVariable("CI_COMMIT_BRANCH")

    /**
     * Provides the commit description (the message body without the first line).
     */
    val commitDescription = providers.environmentVariable("CI_COMMIT_DESCRIPTION")

    /**
     * Provides the full commit message.
     */
    val commitMessage = providers.environmentVariable("CI_COMMIT_MESSAGE")

    /**
     * Provides the branch or tag name for which the project is built.
     */
    val commitRefName = providers.environmentVariable("CI_COMMIT_REF_NAME")

    /**
     * Provides whether the job is running for a protected reference.
     */
    val commitRefProtected = providers.environmentVariable("CI_COMMIT_REF_PROTECTED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the branch or tag name, lowercased and shortened to 63 bytes, suitable for use in URLs and hostnames.
     */
    val commitRefSlug = providers.environmentVariable("CI_COMMIT_REF_SLUG")

    /**
     * Provides the commit SHA the project is built for.
     */
    val commitSha = providers.environmentVariable("CI_COMMIT_SHA")

    /**
     * Provides the first eight characters of the commit SHA.
     */
    val commitShortSha = providers.environmentVariable("CI_COMMIT_SHORT_SHA")

    /**
     * Provides the commit tag name. Only available in tag pipelines.
     */
    val commitTag = providers.environmentVariable("CI_COMMIT_TAG")

    /**
     * Provides the tag message. Only available in tag pipelines.
     */
    val commitTagMessage = providers.environmentVariable("CI_COMMIT_TAG_MESSAGE")

    /**
     * Provides the commit timestamp in ISO 8601 format.
     */
    val commitTimestamp = providers.environmentVariable("CI_COMMIT_TIMESTAMP")

    /**
     * Provides the first line of the commit message.
     */
    val commitTitle = providers.environmentVariable("CI_COMMIT_TITLE")

    // endregion

    // region Configuration

    /**
     * Provides the path to the CI/CD configuration file (defaults to `.gitlab-ci.yml`).
     */
    val configPath = providers.environmentVariable("CI_CONFIG_PATH")

    /**
     * Provides whether debug logging (tracing) is enabled.
     */
    val debugTrace = providers.environmentVariable("CI_DEBUG_TRACE")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides whether service container logging is enabled.
     */
    val debugServices = providers.environmentVariable("CI_DEBUG_SERVICES")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the project's default branch name.
     */
    val defaultBranch = providers.environmentVariable("CI_DEFAULT_BRANCH")

    /**
     * Provides the host of the registry used by CI/CD templates (defaults to `registry.gitlab.com`).
     */
    val templateRegistryHost = providers.environmentVariable("CI_TEMPLATE_REGISTRY_HOST")

    // endregion

    // region Build / Executor

    /**
     * Provides whether the job is executed in a disposable environment (created and destroyed per job). `true` for all
     * executors except `shell` and `ssh`.
     */
    val disposableEnvironment = providers.environmentVariable("CI_DISPOSABLE_ENVIRONMENT")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides whether the job is executed in a shared environment that persists across invocations. `true` for
     * `shell` and `ssh` executors.
     */
    val sharedEnvironment = providers.environmentVariable("CI_SHARED_ENVIRONMENT")
        .map(String::toBooleanStrictOrNull)

    // endregion

    // region Deploy

    /**
     * Provides whether the pipeline runs during a deploy freeze window.
     */
    val deployFreeze = providers.environmentVariable("CI_DEPLOY_FREEZE")
        .map(String::toBooleanStrictOrNull)

    // endregion

    // region Environment

    /**
     * Provides the ID of the environment for the job. Only set if `environment:name` is configured.
     */
    val environmentId = providers.environmentVariable("CI_ENVIRONMENT_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the name of the environment for the job. Only set if `environment:name` is configured.
     */
    val environmentName = providers.environmentVariable("CI_ENVIRONMENT_NAME")

    /**
     * Provides a simplified environment name suitable for DNS, URLs, and Kubernetes labels.
     */
    val environmentSlug = providers.environmentVariable("CI_ENVIRONMENT_SLUG")

    /**
     * Provides the URL of the environment for the job. Only set if `environment:url` is configured.
     */
    val environmentUrl = providers.environmentVariable("CI_ENVIRONMENT_URL")

    /**
     * Provides the action annotation for the job's environment (`start`, `prepare`, or `stop`).
     */
    val environmentAction = providers.environmentVariable("CI_ENVIRONMENT_ACTION")

    /**
     * Provides the deployment tier of the environment.
     */
    val environmentTier = providers.environmentVariable("CI_ENVIRONMENT_TIER")

    // endregion

    // region FIPS

    /**
     * Provides whether FIPS mode is enabled on the GitLab instance.
     */
    val fipsMode = providers.environmentVariable("CI_GITLAB_FIPS_MODE")
        .map(String::toBooleanStrictOrNull)

    // endregion

    // region Job

    /**
     * Provides whether the project has open requirements.
     */
    val hasOpenRequirements = providers.environmentVariable("CI_HAS_OPEN_REQUIREMENTS")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the internal ID of the job, unique across the entire GitLab instance.
     */
    val jobId = providers.environmentVariable("CI_JOB_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the name of the Docker image running the job. Only set if the job explicitly specifies an image.
     */
    val jobImage = providers.environmentVariable("CI_JOB_IMAGE")

    /**
     * Provides whether the job was started manually.
     */
    val jobManual = providers.environmentVariable("CI_JOB_MANUAL")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the name of the job.
     */
    val jobName = providers.environmentVariable("CI_JOB_NAME")

    /**
     * Provides the job name lowercased and shortened to 63 bytes.
     */
    val jobNameSlug = providers.environmentVariable("CI_JOB_NAME_SLUG")

    /**
     * Provides the name of the job's stage.
     */
    val jobStage = providers.environmentVariable("CI_JOB_STAGE")

    /**
     * Provides the status of the job as the runner executes stages (`success`, `failed`, or `canceled`). Intended for
     * use with `after_script`.
     */
    val jobStatus = providers.environmentVariable("CI_JOB_STATUS")

    /**
     * Provides the job timeout in seconds.
     */
    val jobTimeout = providers.environmentVariable("CI_JOB_TIMEOUT")
        .map(String::toIntOrNull)

    /**
     * Provides the URL of the job details page.
     */
    val jobUrl = providers.environmentVariable("CI_JOB_URL")

    /**
     * Provides the date and time when the job started, in ISO 8601 format (UTC).
     */
    val jobStartedAt = providers.environmentVariable("CI_JOB_STARTED_AT")

    /**
     * Provides the index of the job in a parallel job set. Only set if the job uses `parallel`.
     */
    val nodeIndex = providers.environmentVariable("CI_NODE_INDEX")
        .map(String::toIntOrNull)

    /**
     * Provides the total number of instances running in parallel. `1` if `parallel` is not used.
     */
    val nodeTotal = providers.environmentVariable("CI_NODE_TOTAL")
        .map(String::toIntOrNull)

    // endregion

    // region Kubernetes

    /**
     * Provides whether the pipeline has a Kubernetes cluster available for deployments.
     */
    val kubernetesActive = providers.environmentVariable("CI_KUBERNETES_ACTIVE")
        .map(String::toBooleanStrictOrNull)

    // endregion

    // region Pages

    /**
     * Provides the instance's domain hosting GitLab Pages.
     */
    val pagesDomain = providers.environmentVariable("CI_PAGES_DOMAIN")

    /**
     * Provides the URL for the GitLab Pages site.
     */
    val pagesUrl = providers.environmentVariable("CI_PAGES_URL")

    // endregion

    // region Pipeline

    /**
     * Provides the instance-level ID of the current pipeline, unique across all projects.
     */
    val pipelineId = providers.environmentVariable("CI_PIPELINE_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the project-level IID of the current pipeline, unique only within the project.
     */
    val pipelineIid = providers.environmentVariable("CI_PIPELINE_IID")
        .map(String::toLongOrNull)

    /**
     * Provides how the pipeline was triggered (e.g. `push`, `merge_request_event`, `schedule`, `web`, `api`).
     */
    val pipelineSource = providers.environmentVariable("CI_PIPELINE_SOURCE")

    /**
     * Provides whether the pipeline was triggered with a trigger token.
     */
    val pipelineTriggered = providers.environmentVariable("CI_PIPELINE_TRIGGERED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the URL for the pipeline details page.
     */
    val pipelineUrl = providers.environmentVariable("CI_PIPELINE_URL")

    /**
     * Provides the date and time when the pipeline was created, in ISO 8601 format (UTC).
     */
    val pipelineCreatedAt = providers.environmentVariable("CI_PIPELINE_CREATED_AT")

    /**
     * Provides the pipeline name as defined in `workflow:name`.
     */
    val pipelineName = providers.environmentVariable("CI_PIPELINE_NAME")

    /**
     * Provides a comma-separated list of up to four merge requests using the current branch and project.
     */
    val openMergeRequests = providers.environmentVariable("CI_OPEN_MERGE_REQUESTS")

    // endregion

    // region Project

    /**
     * Provides the path where the repository is cloned on the runner.
     */
    val projectDirPath = providers.environmentVariable("CI_PROJECT_DIR")

    /**
     * Provides the directory where the repository is cloned on the runner, as a [org.gradle.api.file.Directory].
     */
    val projectDir = layout.dir(projectDirPath.map(::File))

    /**
     * Provides the ID of the current project, unique across all projects on the instance.
     */
    val projectId = providers.environmentVariable("CI_PROJECT_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the directory name of the project (e.g. `my-project`).
     */
    val projectName = providers.environmentVariable("CI_PROJECT_NAME")

    /**
     * Provides the project namespace (username or group name).
     */
    val projectNamespace = providers.environmentVariable("CI_PROJECT_NAMESPACE")

    /**
     * Provides the project namespace ID.
     */
    val projectNamespaceId = providers.environmentVariable("CI_PROJECT_NAMESPACE_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the project namespace with the project name included (e.g. `my-group/my-project`).
     */
    val projectPath = providers.environmentVariable("CI_PROJECT_PATH")

    /**
     * Provides the project path lowercased and shortened to 63 bytes, suitable for URLs and hostnames.
     */
    val projectPathSlug = providers.environmentVariable("CI_PROJECT_PATH_SLUG")

    /**
     * Provides a comma-separated lowercase list of the repository's programming languages (maximum 5).
     */
    val projectRepositoryLanguages = providers.environmentVariable("CI_PROJECT_REPOSITORY_LANGUAGES")

    /**
     * Provides the root-level group namespace.
     */
    val projectRootNamespace = providers.environmentVariable("CI_PROJECT_ROOT_NAMESPACE")

    /**
     * Provides the human-readable project name as displayed in the UI.
     */
    val projectTitle = providers.environmentVariable("CI_PROJECT_TITLE")

    /**
     * Provides the project description as displayed in the UI.
     */
    val projectDescription = providers.environmentVariable("CI_PROJECT_DESCRIPTION")

    /**
     * Provides the HTTP(S) address of the project.
     */
    val projectUrl = providers.environmentVariable("CI_PROJECT_URL")

    /**
     * Provides the project visibility (`internal`, `private`, or `public`).
     */
    val projectVisibility = providers.environmentVariable("CI_PROJECT_VISIBILITY")

    /**
     * Provides the project's external authorization classification label.
     */
    val projectClassificationLabel = providers.environmentVariable("CI_PROJECT_CLASSIFICATION_LABEL")

    // endregion

    // region Registry

    /**
     * Provides the address of the container registry server. Only set if the registry is enabled.
     */
    val registry = providers.environmentVariable("CI_REGISTRY")

    /**
     * Provides the base address for the project's container registry. Only set if the registry is enabled.
     */
    val registryImage = providers.environmentVariable("CI_REGISTRY_IMAGE")

    // endregion

    // region Release

    /**
     * Provides the description of the release (first 1024 characters). Only available in tag pipelines.
     */
    val releaseDescription = providers.environmentVariable("CI_RELEASE_DESCRIPTION")

    // endregion

    // region Runner

    /**
     * Provides the description of the runner.
     */
    val runnerDescription = providers.environmentVariable("CI_RUNNER_DESCRIPTION")

    /**
     * Provides the OS and architecture of the GitLab Runner executable.
     */
    val runnerExecutableArch = providers.environmentVariable("CI_RUNNER_EXECUTABLE_ARCH")

    /**
     * Provides the unique ID of the runner being used.
     */
    val runnerId = providers.environmentVariable("CI_RUNNER_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the revision of the runner running the job.
     */
    val runnerRevision = providers.environmentVariable("CI_RUNNER_REVISION")

    /**
     * Provides the first 17 characters of the runner's authentication token.
     */
    val runnerShortToken = providers.environmentVariable("CI_RUNNER_SHORT_TOKEN")

    /**
     * Provides the runner's tags as a JSON array string (e.g. `["docker", "linux"]`).
     */
    val runnerTags = providers.environmentVariable("CI_RUNNER_TAGS")

    /**
     * Provides the version of the GitLab Runner running the job.
     */
    val runnerVersion = providers.environmentVariable("CI_RUNNER_VERSION")

    // endregion

    // region Dependency Proxy

    /**
     * Provides the direct group image prefix for pulling images through the Dependency Proxy.
     */
    val dependencyProxyDirectGroupImagePrefix =
        providers.environmentVariable("CI_DEPENDENCY_PROXY_DIRECT_GROUP_IMAGE_PREFIX")

    /**
     * Provides the top-level group image prefix for pulling images through the Dependency Proxy.
     */
    val dependencyProxyGroupImagePrefix =
        providers.environmentVariable("CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX")

    /**
     * Provides the server address for logging into the Dependency Proxy.
     */
    val dependencyProxyServer = providers.environmentVariable("CI_DEPENDENCY_PROXY_SERVER")

    // endregion

    // region User

    /**
     * Provides the email of the user who started the pipeline.
     */
    val userEmail = providers.environmentVariable("GITLAB_USER_EMAIL")

    /**
     * Provides the numeric ID of the user who started the pipeline.
     */
    val userId = providers.environmentVariable("GITLAB_USER_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the unique username of the user who started the pipeline.
     */
    val userLogin = providers.environmentVariable("GITLAB_USER_LOGIN")

    /**
     * Provides the display name of the user who started the pipeline.
     */
    val userName = providers.environmentVariable("GITLAB_USER_NAME")

    // endregion
}
