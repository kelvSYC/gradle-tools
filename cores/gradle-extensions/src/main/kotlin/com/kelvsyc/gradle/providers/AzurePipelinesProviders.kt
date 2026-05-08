package com.kelvsyc.gradle.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to Azure Pipelines.
 *
 * See [Azure Pipelines Predefined Variables](https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables).
 * Sensitive variables (`System.AccessToken`, `System.OidcRequestUri`) and deprecated variables are intentionally
 * excluded.
 */
@Suppress("TooManyFunctions")
abstract class AzurePipelinesProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {

    // region General

    /**
     * Provides `true` if a build task runs the script. Defaults to `false` when the `TF_BUILD` environment variable is
     * absent.
     */
    val tfBuild = providers.environmentVariable("TF_BUILD")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides the workspace directory for the pipeline. Same value as [agentBuildDirectoryPath].
     */
    val pipelineWorkspacePath = providers.environmentVariable("PIPELINE_WORKSPACE")

    /**
     * Provides the workspace directory for the pipeline, as a [org.gradle.api.file.Directory].
     */
    val pipelineWorkspace = layout.dir(pipelineWorkspacePath.map(::File))

    // endregion

    // region Agent

    /**
     * Provides the local path on the agent where all folders for the build pipeline are created.
     */
    val agentBuildDirectoryPath = providers.environmentVariable("AGENT_BUILDDIRECTORY")

    /**
     * Provides the directory on the agent where all folders for the build pipeline are created, as a
     * [org.gradle.api.file.Directory].
     */
    val agentBuildDirectory = layout.dir(agentBuildDirectoryPath.map(::File))

    /**
     * Provides the directory where the agent is installed.
     */
    val agentHomeDirectoryPath = providers.environmentVariable("AGENT_HOMEDIRECTORY")

    /**
     * Provides the ID of the agent.
     */
    val agentId = providers.environmentVariable("AGENT_ID")
        .map(String::toIntOrNull)

    /**
     * Provides the name of the running job.
     */
    val agentJobName = providers.environmentVariable("AGENT_JOBNAME")

    /**
     * Provides the status of the build (`Canceled`, `Failed`, `Succeeded`, `SucceededWithIssues`, or `Skipped`).
     */
    val agentJobStatus = providers.environmentVariable("AGENT_JOBSTATUS")

    /**
     * Provides the name of the machine on which the agent is installed.
     */
    val agentMachineName = providers.environmentVariable("AGENT_MACHINENAME")

    /**
     * Provides the name of the agent registered with the pool.
     */
    val agentName = providers.environmentVariable("AGENT_NAME")

    /**
     * Provides the operating system of the agent host (`Windows_NT`, `Darwin`, or `Linux`).
     */
    val agentOs = providers.environmentVariable("AGENT_OS")

    /**
     * Provides the processor architecture of the agent host (`X86`, `X64`, or `ARM`).
     */
    val agentOsArchitecture = providers.environmentVariable("AGENT_OSARCHITECTURE")

    /**
     * Provides the path to a temporary folder on the agent, cleaned after each pipeline job.
     */
    val agentTempDirectoryPath = providers.environmentVariable("AGENT_TEMPDIRECTORY")

    /**
     * Provides the temporary folder on the agent, as a [org.gradle.api.file.Directory].
     */
    val agentTempDirectory = layout.dir(agentTempDirectoryPath.map(::File))

    /**
     * Provides the directory used by tool installer tasks to switch between tool versions.
     */
    val agentToolsDirectoryPath = providers.environmentVariable("AGENT_TOOLSDIRECTORY")

    /**
     * Provides the tool installer directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val agentToolsDirectory = layout.dir(agentToolsDirectoryPath.map(::File))

    /**
     * Provides the working directory for the agent.
     */
    val agentWorkFolderPath = providers.environmentVariable("AGENT_WORKFOLDER")

    // endregion

    // region Build

    /**
     * Provides the local path on the agent where artifacts are copied before being pushed to their destination.
     */
    val artifactStagingDirectoryPath = providers.environmentVariable("BUILD_ARTIFACTSTAGINGDIRECTORY")

    /**
     * Provides the artifact staging directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val artifactStagingDirectory = layout.dir(artifactStagingDirectoryPath.map(::File))

    /**
     * Provides the ID of the record for the completed build.
     */
    val buildId = providers.environmentVariable("BUILD_BUILDID")
        .map(String::toLongOrNull)

    /**
     * Provides the name of the completed build (the run number).
     */
    val buildNumber = providers.environmentVariable("BUILD_BUILDNUMBER")

    /**
     * Provides the URI for the build.
     */
    val buildUri = providers.environmentVariable("BUILD_BUILDURI")

    /**
     * Provides the local path on the agent usable as an output folder for compiled binaries.
     */
    val binariesDirectoryPath = providers.environmentVariable("BUILD_BINARIESDIRECTORY")

    /**
     * Provides the binaries output directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val binariesDirectory = layout.dir(binariesDirectoryPath.map(::File))

    /**
     * Provides the `displayName` of the cron schedule that triggered the pipeline run. Only set for scheduled
     * triggers.
     */
    val cronScheduleDisplayName = providers.environmentVariable("BUILD_CRONSCHEDULE_DISPLAYNAME")

    /**
     * Provides the name of the build pipeline.
     */
    val definitionName = providers.environmentVariable("BUILD_DEFINITIONNAME")

    /**
     * Provides the version of the build pipeline.
     */
    val definitionVersion = providers.environmentVariable("BUILD_DEFINITIONVERSION")
        .map(String::toIntOrNull)

    /**
     * Provides the display name of the person who queued the build.
     */
    val queuedBy = providers.environmentVariable("BUILD_QUEUEDBY")

    /**
     * Provides the ID of the person who queued the build.
     */
    val queuedById = providers.environmentVariable("BUILD_QUEUEDBYID")

    /**
     * Provides the event that caused the build to run (e.g. `Manual`, `IndividualCI`, `BatchedCI`, `Schedule`,
     * `PullRequest`, `BuildCompletion`, `ResourceTrigger`).
     */
    val buildReason = providers.environmentVariable("BUILD_REASON")

    /**
     * Provides the display name of the person the build was requested for.
     */
    val requestedFor = providers.environmentVariable("BUILD_REQUESTEDFOR")

    /**
     * Provides the email of the person the build was requested for.
     */
    val requestedForEmail = providers.environmentVariable("BUILD_REQUESTEDFOREMAIL")

    /**
     * Provides the ID of the person the build was requested for.
     */
    val requestedForId = providers.environmentVariable("BUILD_REQUESTEDFORID")

    // endregion

    // region Source Control

    /**
     * Provides the unique identifier of the triggering repository.
     */
    val repositoryId = providers.environmentVariable("BUILD_REPOSITORY_ID")

    /**
     * Provides the name of the triggering repository.
     */
    val repositoryName = providers.environmentVariable("BUILD_REPOSITORY_NAME")

    /**
     * Provides the type of the triggering repository (`TfsGit`, `TfsVersionControl`, `Git`, `GitHub`, or `Svn`).
     */
    val repositoryProvider = providers.environmentVariable("BUILD_REPOSITORY_PROVIDER")

    /**
     * Provides the URL for the triggering repository.
     */
    val repositoryUri = providers.environmentVariable("BUILD_REPOSITORY_URI")

    /**
     * Provides the local path on the agent where source code files are downloaded from the triggering repository.
     */
    val repositoryLocalPath = providers.environmentVariable("BUILD_REPOSITORY_LOCALPATH")

    /**
     * Provides the value of the "Checkout submodules" setting from the repository tab. Git only.
     */
    val repositoryGitSubmoduleCheckout = providers.environmentVariable("BUILD_REPOSITORY_GIT_SUBMODULECHECKOUT")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the branch of the triggering repo the build was queued for (e.g. `refs/heads/main`,
     * `refs/pull/1/merge`).
     */
    val sourceBranch = providers.environmentVariable("BUILD_SOURCEBRANCH")

    /**
     * Provides the short name of the branch (last path segment, e.g. `main`).
     */
    val sourceBranchName = providers.environmentVariable("BUILD_SOURCEBRANCHNAME")

    /**
     * Provides the local path on the agent where source code files are downloaded.
     */
    val sourcesDirectoryPath = providers.environmentVariable("BUILD_SOURCESDIRECTORY")

    /**
     * Provides the sources directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val sourcesDirectory = layout.dir(sourcesDirectoryPath.map(::File))

    /**
     * Provides the latest version control change included in this build (commit ID for Git, changeset for TFVC).
     */
    val sourceVersion = providers.environmentVariable("BUILD_SOURCEVERSION")

    /**
     * Provides the comment of the commit or changeset, truncated to the first line or 200 characters.
     */
    val sourceVersionMessage = providers.environmentVariable("BUILD_SOURCEVERSIONMESSAGE")

    /**
     * Provides the value selected for Clean in the source repository settings.
     */
    val repositoryClean = providers.environmentVariable("BUILD_REPOSITORY_CLEAN")
        .map(String::toBooleanStrictOrNull)

    // endregion

    // region Triggered By

    /**
     * Provides the BuildID of the triggering build, if this build was triggered by another build.
     */
    val triggeredByBuildId = providers.environmentVariable("BUILD_TRIGGEREDBY_BUILDID")
        .map(String::toLongOrNull)

    /**
     * Provides the DefinitionID of the triggering build pipeline, if this build was triggered by another build.
     */
    val triggeredByDefinitionId = providers.environmentVariable("BUILD_TRIGGEREDBY_DEFINITIONID")
        .map(String::toIntOrNull)

    /**
     * Provides the name of the triggering build pipeline, if this build was triggered by another build.
     */
    val triggeredByDefinitionName = providers.environmentVariable("BUILD_TRIGGEREDBY_DEFINITIONNAME")

    /**
     * Provides the number of the triggering build, if this build was triggered by another build.
     */
    val triggeredByBuildNumber = providers.environmentVariable("BUILD_TRIGGEREDBY_BUILDNUMBER")

    /**
     * Provides the ID of the project containing the triggering build, if this build was triggered by another build.
     */
    val triggeredByProjectId = providers.environmentVariable("BUILD_TRIGGEREDBY_PROJECTID")

    // endregion

    // region Stage

    /**
     * Provides the display name of the person who triggered the stage manually, or
     * `Microsoft.VisualStudio.Services.TFS` otherwise.
     */
    val stageRequestedBy = providers.environmentVariable("BUILD_STAGEREQUESTBY")

    /**
     * Provides the GUID of the person who triggered the stage manually.
     */
    val stageRequestedForId = providers.environmentVariable("BUILD_STAGEREQUESTFORID")

    /**
     * Provides the attempt number for the current stage, starting at 1.
     */
    val stageAttempt = providers.environmentVariable("SYSTEM_STAGEATTEMPT")
        .map(String::toIntOrNull)

    /**
     * Provides the human-readable name given to the current stage.
     */
    val stageDisplayName = providers.environmentVariable("SYSTEM_STAGEDISPLAYNAME")

    /**
     * Provides the identifier for the current stage, used for expressing dependencies and accessing output variables.
     */
    val stageName = providers.environmentVariable("SYSTEM_STAGENAME")

    // endregion

    // region System

    /**
     * Provides the GUID of the Azure DevOps organization or collection.
     */
    val collectionId = providers.environmentVariable("SYSTEM_COLLECTIONID")

    /**
     * Provides the URI of the Azure DevOps organization or collection.
     */
    val collectionUri = providers.environmentVariable("SYSTEM_COLLECTIONURI")

    /**
     * Provides whether detailed debug logging is enabled for the pipeline.
     */
    val debug = providers.environmentVariable("SYSTEM_DEBUG")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the default working directory on the agent where source code files are downloaded.
     */
    val defaultWorkingDirectoryPath = providers.environmentVariable("SYSTEM_DEFAULTWORKINGDIRECTORY")

    /**
     * Provides the default working directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val defaultWorkingDirectory = layout.dir(defaultWorkingDirectoryPath.map(::File))

    /**
     * Provides the ID of the build pipeline.
     */
    val systemDefinitionId = providers.environmentVariable("SYSTEM_DEFINITIONID")
        .map(String::toIntOrNull)

    /**
     * Provides the host type (`build` for build pipelines, `deployment`/`gates`/`release` for release pipelines).
     */
    val hostType = providers.environmentVariable("SYSTEM_HOSTTYPE")

    /**
     * Provides the URI of the Azure DevOps organization or collection. Equivalent to [collectionUri].
     */
    val teamFoundationCollectionUri = providers.environmentVariable("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI")

    /**
     * Provides the name of the project that contains this build.
     */
    val teamProject = providers.environmentVariable("SYSTEM_TEAMPROJECT")

    /**
     * Provides the ID of the project that this build belongs to.
     */
    val teamProjectId = providers.environmentVariable("SYSTEM_TEAMPROJECTID")

    // endregion

    // region Job

    /**
     * Provides the attempt number for the current job, starting at 1.
     */
    val jobAttempt = providers.environmentVariable("SYSTEM_JOBATTEMPT")
        .map(String::toIntOrNull)

    /**
     * Provides the human-readable name given to the current job.
     */
    val jobDisplayName = providers.environmentVariable("SYSTEM_JOBDISPLAYNAME")

    /**
     * Provides a unique identifier for a single attempt of a single job.
     */
    val jobId = providers.environmentVariable("SYSTEM_JOBID")

    /**
     * Provides the name of the job, used for expressing dependencies and accessing output variables.
     */
    val jobName = providers.environmentVariable("SYSTEM_JOBNAME")

    // endregion

    // region Pull Request

    /**
     * Provides whether the pull request is from a fork of the repository.
     */
    val pullRequestIsFork = providers.environmentVariable("SYSTEM_PULLREQUEST_ISFORK")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the ID of the pull request that caused this build. Only set for builds caused by a Git PR affected by a
     * branch policy.
     */
    val pullRequestId = providers.environmentVariable("SYSTEM_PULLREQUEST_PULLREQUESTID")
        .map(String::toIntOrNull)

    /**
     * Provides the number of the pull request. Populated for GitHub pull requests where the PR ID and PR number differ.
     */
    val pullRequestNumber = providers.environmentVariable("SYSTEM_PULLREQUEST_PULLREQUESTNUMBER")
        .map(String::toIntOrNull)

    /**
     * Provides the source branch of the pull request (e.g. `refs/heads/users/raisa/new-feature`).
     */
    val pullRequestSourceBranch = providers.environmentVariable("SYSTEM_PULLREQUEST_SOURCEBRANCH")

    /**
     * Provides the commit ID being reviewed in the pull request.
     */
    val pullRequestSourceCommitId = providers.environmentVariable("SYSTEM_PULLREQUEST_SOURCECOMMITID")

    /**
     * Provides the URL of the repository that contains the pull request.
     */
    val pullRequestSourceRepositoryUri = providers.environmentVariable("SYSTEM_PULLREQUEST_SOURCEREPOSITORYURI")

    /**
     * Provides the target branch of the pull request (e.g. `refs/heads/main` for Azure Repos, `main` for GitHub).
     */
    val pullRequestTargetBranch = providers.environmentVariable("SYSTEM_PULLREQUEST_TARGETBRANCH")

    /**
     * Provides the name of the target branch without the `refs/heads/` prefix.
     */
    val pullRequestTargetBranchName = providers.environmentVariable("SYSTEM_PULLREQUEST_TARGETBRANCHNAME")

    // endregion

    // region Deployment

    /**
     * Provides the name of the environment targeted in the deployment job.
     */
    val environmentName = providers.environmentVariable("ENVIRONMENT_NAME")

    /**
     * Provides the ID of the environment targeted in the deployment job.
     */
    val environmentId = providers.environmentVariable("ENVIRONMENT_ID")
        .map(String::toIntOrNull)

    /**
     * Provides the name of the specific resource within the environment targeted in the deployment job.
     */
    val environmentResourceName = providers.environmentVariable("ENVIRONMENT_RESOURCENAME")

    /**
     * Provides the ID of the specific resource within the environment targeted in the deployment job.
     */
    val environmentResourceId = providers.environmentVariable("ENVIRONMENT_RESOURCEID")
        .map(String::toIntOrNull)

    /**
     * Provides the name of the deployment strategy (`canary`, `runOnce`, or `rolling`).
     */
    val strategyName = providers.environmentVariable("STRATEGY_NAME")

    /**
     * Provides the current cycle name in a deployment (`PreIteration`, `Iteration`, or `PostIteration`).
     */
    val strategyCycleName = providers.environmentVariable("STRATEGY_CYCLENAME")

    // endregion

    // region Common

    /**
     * Provides the local path on the agent where test results are created.
     */
    val testResultsDirectoryPath = providers.environmentVariable("COMMON_TESTRESULTSDIRECTORY")

    /**
     * Provides the test results directory on the agent, as a [org.gradle.api.file.Directory].
     */
    val testResultsDirectory = layout.dir(testResultsDirectoryPath.map(::File))

    // endregion
}
