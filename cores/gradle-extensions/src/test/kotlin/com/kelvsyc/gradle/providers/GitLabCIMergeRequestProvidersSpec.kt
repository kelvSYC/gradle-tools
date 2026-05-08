package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.gitlabMergeRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class GitLabCIMergeRequestProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.gitlabMergeRequest

            providers.shouldNotBeNull()
        }

        test("approved is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_APPROVED") == null) {
                providers.approved.orNull.shouldBeNull()
            }
        }

        test("id is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_ID") == null) {
                providers.id.orNull.shouldBeNull()
            }
        }

        test("iid is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_IID") == null) {
                providers.iid.orNull.shouldBeNull()
            }
        }

        test("draft is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_DRAFT") == null) {
                providers.draft.orNull.shouldBeNull()
            }
        }

        test("sourceBranchProtected is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_SOURCE_BRANCH_PROTECTED") == null) {
                providers.sourceBranchProtected.orNull.shouldBeNull()
            }
        }

        test("targetBranchProtected is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_TARGET_BRANCH_PROTECTED") == null) {
                providers.targetBranchProtected.orNull.shouldBeNull()
            }
        }

        test("squashOnMerge is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_SQUASH_ON_MERGE") == null) {
                providers.squashOnMerge.orNull.shouldBeNull()
            }
        }

        test("diffId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_DIFF_ID") == null) {
                providers.diffId.orNull.shouldBeNull()
            }
        }

        test("projectId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_PROJECT_ID") == null) {
                providers.projectId.orNull.shouldBeNull()
            }
        }

        test("sourceProjectId is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            if (System.getenv("CI_MERGE_REQUEST_SOURCE_PROJECT_ID") == null) {
                providers.sourceProjectId.orNull.shouldBeNull()
            }
        }

        test("title provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            providers.title.shouldNotBeNull()
        }

        test("sourceBranchName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            providers.sourceBranchName.shouldNotBeNull()
        }

        test("targetBranchName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            providers.targetBranchName.shouldNotBeNull()
        }

        test("eventType provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.gitlabMergeRequest

            providers.eventType.shouldNotBeNull()
        }
    }
}
