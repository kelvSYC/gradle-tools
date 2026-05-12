package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ModelDeserializationSpec : FunSpec() {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    init {
        context("Account") {
            test("deserializes with @Json-mapped fields") {
                val json = """
                    {
                        "uuid": "{abc-123}",
                        "display_name": "Jane Doe",
                        "type": "user",
                        "nickname": "jdoe"
                    }
                """.trimIndent()

                val account = moshi.adapter(Account::class.java).fromJson(json)!!

                account.uuid shouldBe "{abc-123}"
                account.displayName shouldBe "Jane Doe"
                account.type shouldBe "user"
                account.nickname shouldBe "jdoe"
            }

            test("handles missing optional fields") {
                val json = """{"uuid": "{abc-123}"}"""

                val account = moshi.adapter(Account::class.java).fromJson(json)!!

                account.uuid shouldBe "{abc-123}"
                account.displayName.shouldBeNull()
                account.type.shouldBeNull()
                account.nickname.shouldBeNull()
            }
        }

        context("Repository") {
            test("deserializes full repository response") {
                val json = """
                    {
                        "uuid": "{repo-uuid}",
                        "name": "my-repo",
                        "slug": "my-repo",
                        "full_name": "workspace/my-repo",
                        "description": "A test repo",
                        "is_private": true,
                        "mainbranch": {"name": "main", "type": "branch"},
                        "owner": {"uuid": "{owner-uuid}", "display_name": "Owner"},
                        "project": {"uuid": "{proj-uuid}", "key": "PROJ", "name": "Project"},
                        "language": "kotlin",
                        "created_on": "2024-01-01T00:00:00Z",
                        "updated_on": "2024-06-01T00:00:00Z",
                        "type": "repository"
                    }
                """.trimIndent()

                val repo = moshi.adapter(Repository::class.java).fromJson(json)!!

                repo.fullName shouldBe "workspace/my-repo"
                repo.isPrivate shouldBe true
                repo.mainBranch?.name shouldBe "main"
                repo.owner?.displayName shouldBe "Owner"
                repo.project?.key shouldBe "PROJ"
                repo.createdOn shouldBe "2024-01-01T00:00:00Z"
                repo.updatedOn shouldBe "2024-06-01T00:00:00Z"
            }
        }

        context("PullRequest") {
            test("deserializes with nested endpoints and @Json-mapped fields") {
                val json = """
                    {
                        "id": 42,
                        "title": "Add feature",
                        "state": "OPEN",
                        "source": {
                            "branch": {"name": "feature"},
                            "repository": {"full_name": "ws/repo"},
                            "commit": {"hash": "abc123"}
                        },
                        "destination": {
                            "branch": {"name": "main"}
                        },
                        "author": {"display_name": "Dev"},
                        "close_source_branch": true,
                        "merge_commit": {"hash": "def456"},
                        "created_on": "2024-01-01T00:00:00Z",
                        "updated_on": "2024-01-02T00:00:00Z",
                        "type": "pullrequest"
                    }
                """.trimIndent()

                val pr = moshi.adapter(PullRequest::class.java).fromJson(json)!!

                pr.id shouldBe 42
                pr.title shouldBe "Add feature"
                pr.state shouldBe "OPEN"
                pr.source?.branch?.name shouldBe "feature"
                pr.source?.repository?.fullName shouldBe "ws/repo"
                pr.source?.commit?.hash shouldBe "abc123"
                pr.destination?.branch?.name shouldBe "main"
                pr.author?.displayName shouldBe "Dev"
                pr.closeSourceBranch shouldBe true
                pr.mergeCommit?.hash shouldBe "def456"
                pr.createdOn shouldBe "2024-01-01T00:00:00Z"
                pr.updatedOn shouldBe "2024-01-02T00:00:00Z"
            }
        }

        context("CommitStatus") {
            test("deserializes with @Json-mapped fields") {
                val json = """
                    {
                        "state": "SUCCESSFUL",
                        "key": "build-123",
                        "name": "CI Build",
                        "url": "https://ci.example.com/build/123",
                        "description": "Build passed",
                        "created_on": "2024-01-01T00:00:00Z",
                        "updated_on": "2024-01-01T01:00:00Z",
                        "type": "build"
                    }
                """.trimIndent()

                val status = moshi.adapter(CommitStatus::class.java).fromJson(json)!!

                status.state shouldBe "SUCCESSFUL"
                status.key shouldBe "build-123"
                status.name shouldBe "CI Build"
                status.url shouldBe "https://ci.example.com/build/123"
                status.createdOn shouldBe "2024-01-01T00:00:00Z"
                status.updatedOn shouldBe "2024-01-01T01:00:00Z"
            }
        }

        context("PullRequestComment") {
            test("deserializes with nested CommentContent") {
                val json = """
                    {
                        "id": 99,
                        "content": {
                            "raw": "LGTM",
                            "html": "<p>LGTM</p>",
                            "markup": "markdown"
                        },
                        "user": {"display_name": "Reviewer"},
                        "created_on": "2024-01-01T00:00:00Z",
                        "updated_on": "2024-01-02T00:00:00Z",
                        "type": "pullrequest_comment"
                    }
                """.trimIndent()

                val comment = moshi.adapter(PullRequestComment::class.java).fromJson(json)!!

                comment.id shouldBe 99
                comment.content?.raw shouldBe "LGTM"
                comment.content?.html shouldBe "<p>LGTM</p>"
                comment.content?.markup shouldBe "markdown"
                comment.user?.displayName shouldBe "Reviewer"
                comment.createdOn shouldBe "2024-01-01T00:00:00Z"
            }
        }

        context("PaginatedResponse") {
            test("deserializes paginated response with values") {
                val json = """
                    {
                        "page": 1,
                        "size": 2,
                        "pagelen": 10,
                        "next": "https://api.bitbucket.org/2.0/next-page",
                        "previous": null,
                        "values": [
                            {"id": 1, "title": "PR 1"},
                            {"id": 2, "title": "PR 2"}
                        ]
                    }
                """.trimIndent()

                val type = Types.newParameterizedType(PaginatedResponse::class.java, PullRequest::class.java)
                val adapter = moshi.adapter<PaginatedResponse<PullRequest>>(type)
                val response = adapter.fromJson(json)!!

                response.page shouldBe 1
                response.size shouldBe 2
                response.pageLen shouldBe 10
                response.next shouldBe "https://api.bitbucket.org/2.0/next-page"
                response.previous.shouldBeNull()
                response.values shouldHaveSize 2
                response.values[0].id shouldBe 1
                response.values[1].title shouldBe "PR 2"
            }

            test("defaults to empty list when values is absent") {
                val json = """{"page": 1}"""

                val type = Types.newParameterizedType(PaginatedResponse::class.java, PullRequest::class.java)
                val adapter = moshi.adapter<PaginatedResponse<PullRequest>>(type)
                val response = adapter.fromJson(json)!!

                response.values shouldHaveSize 0
            }
        }

        context("RepositorySummary") {
            test("deserializes with @Json-mapped fields") {
                val json = """
                    {
                        "uuid": "{repo-uuid}",
                        "name": "my-repo",
                        "full_name": "workspace/my-repo",
                        "type": "repository"
                    }
                """.trimIndent()

                val summary = moshi.adapter(RepositorySummary::class.java).fromJson(json)!!

                summary.uuid shouldBe "{repo-uuid}"
                summary.name shouldBe "my-repo"
                summary.fullName shouldBe "workspace/my-repo"
                summary.type shouldBe "repository"
            }
        }

        context("Branch") {
            test("deserializes branch") {
                val json = """{"name": "main", "type": "branch"}"""

                val branch = moshi.adapter(Branch::class.java).fromJson(json)!!

                branch.name shouldBe "main"
                branch.type shouldBe "branch"
            }
        }

        context("CommitSummary") {
            test("deserializes commit summary") {
                val json = """{"hash": "abc123def", "type": "commit"}"""

                val commit = moshi.adapter(CommitSummary::class.java).fromJson(json)!!

                commit.hash shouldBe "abc123def"
                commit.type shouldBe "commit"
            }
        }

        context("Project") {
            test("deserializes project") {
                val json = """
                    {
                        "uuid": "{proj-uuid}",
                        "key": "PROJ",
                        "name": "My Project",
                        "type": "project"
                    }
                """.trimIndent()

                val project = moshi.adapter(Project::class.java).fromJson(json)!!

                project.uuid shouldBe "{proj-uuid}"
                project.key shouldBe "PROJ"
                project.name shouldBe "My Project"
                project.type shouldBe "project"
            }
        }
    }
}
