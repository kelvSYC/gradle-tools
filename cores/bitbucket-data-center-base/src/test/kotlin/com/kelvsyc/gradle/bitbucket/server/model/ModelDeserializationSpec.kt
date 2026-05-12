package com.kelvsyc.gradle.bitbucket.server.model

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
        context("User") {
            test("deserializes with @Json-mapped fields") {
                val json = """
                    {
                        "id": 1,
                        "name": "jdoe",
                        "displayName": "Jane Doe",
                        "emailAddress": "jdoe@example.com",
                        "slug": "jdoe",
                        "type": "NORMAL",
                        "active": true
                    }
                """.trimIndent()

                val user = moshi.adapter(User::class.java).fromJson(json)!!

                user.id shouldBe 1
                user.name shouldBe "jdoe"
                user.displayName shouldBe "Jane Doe"
                user.emailAddress shouldBe "jdoe@example.com"
                user.slug shouldBe "jdoe"
                user.type shouldBe "NORMAL"
                user.active shouldBe true
            }

            test("handles missing optional fields") {
                val json = """{"id": 1, "name": "jdoe"}"""

                val user = moshi.adapter(User::class.java).fromJson(json)!!

                user.id shouldBe 1
                user.displayName.shouldBeNull()
                user.emailAddress.shouldBeNull()
                user.active.shouldBeNull()
            }
        }

        context("Project") {
            test("deserializes all fields") {
                val json = """
                    {
                        "id": 10,
                        "key": "PROJ",
                        "name": "My Project",
                        "description": "A test project",
                        "public": false,
                        "type": "NORMAL"
                    }
                """.trimIndent()

                val project = moshi.adapter(Project::class.java).fromJson(json)!!

                project.id shouldBe 10
                project.key shouldBe "PROJ"
                project.name shouldBe "My Project"
                project.description shouldBe "A test project"
                project.public shouldBe false
                project.type shouldBe "NORMAL"
            }
        }

        context("Repository") {
            test("deserializes with nested project and @Json-mapped fields") {
                val json = """
                    {
                        "id": 100,
                        "slug": "my-repo",
                        "name": "My Repo",
                        "description": "A test repo",
                        "state": "AVAILABLE",
                        "scmId": "git",
                        "forkable": true,
                        "public": false,
                        "project": {
                            "id": 10,
                            "key": "PROJ",
                            "name": "My Project"
                        }
                    }
                """.trimIndent()

                val repo = moshi.adapter(Repository::class.java).fromJson(json)!!

                repo.id shouldBe 100
                repo.slug shouldBe "my-repo"
                repo.scmId shouldBe "git"
                repo.forkable shouldBe true
                repo.project?.key shouldBe "PROJ"
            }
        }

        context("BuildStatus") {
            test("deserializes with @Json-mapped dateAdded") {
                val json = """
                    {
                        "state": "SUCCESSFUL",
                        "key": "build-123",
                        "name": "CI Build",
                        "url": "https://ci.example.com/build/123",
                        "description": "Build passed",
                        "dateAdded": 1704067200000
                    }
                """.trimIndent()

                val status = moshi.adapter(BuildStatus::class.java).fromJson(json)!!

                status.state shouldBe "SUCCESSFUL"
                status.key shouldBe "build-123"
                status.name shouldBe "CI Build"
                status.url shouldBe "https://ci.example.com/build/123"
                status.description shouldBe "Build passed"
                status.dateAdded shouldBe 1704067200000L
            }
        }

        context("Comment") {
            test("deserializes with nested user and @Json-mapped date fields") {
                val json = """
                    {
                        "id": 42,
                        "text": "LGTM",
                        "author": {
                            "name": "reviewer",
                            "displayName": "Reviewer"
                        },
                        "createdDate": 1704067200000,
                        "updatedDate": 1704153600000,
                        "version": 1
                    }
                """.trimIndent()

                val comment = moshi.adapter(Comment::class.java).fromJson(json)!!

                comment.id shouldBe 42
                comment.text shouldBe "LGTM"
                comment.author?.displayName shouldBe "Reviewer"
                comment.createdDate shouldBe 1704067200000L
                comment.updatedDate shouldBe 1704153600000L
                comment.version shouldBe 1
            }
        }

        context("PullRequest") {
            test("deserializes with nested refs, participants, and @Json-mapped fields") {
                val json = """
                    {
                        "id": 1,
                        "title": "Add feature",
                        "description": "Implements feature X",
                        "state": "OPEN",
                        "fromRef": {
                            "id": "refs/heads/feature",
                            "displayId": "feature",
                            "latestCommit": "abc123"
                        },
                        "toRef": {
                            "id": "refs/heads/main",
                            "displayId": "main",
                            "latestCommit": "def456"
                        },
                        "author": {
                            "user": {"name": "dev", "displayName": "Developer"},
                            "role": "AUTHOR",
                            "approved": false
                        },
                        "reviewers": [
                            {
                                "user": {"name": "rev", "displayName": "Reviewer"},
                                "role": "REVIEWER",
                                "approved": true,
                                "status": "APPROVED"
                            }
                        ],
                        "open": true,
                        "closed": false,
                        "createdDate": 1704067200000,
                        "updatedDate": 1704153600000
                    }
                """.trimIndent()

                val pr = moshi.adapter(PullRequest::class.java).fromJson(json)!!

                pr.id shouldBe 1
                pr.title shouldBe "Add feature"
                pr.state shouldBe "OPEN"
                pr.fromRef?.displayId shouldBe "feature"
                pr.fromRef?.latestCommit shouldBe "abc123"
                pr.toRef?.displayId shouldBe "main"
                pr.author?.user?.displayName shouldBe "Developer"
                pr.author?.role shouldBe "AUTHOR"
                pr.reviewers!! shouldHaveSize 1
                pr.reviewers!![0].approved shouldBe true
                pr.reviewers!![0].status shouldBe "APPROVED"
                pr.open shouldBe true
                pr.closed shouldBe false
                pr.createdDate shouldBe 1704067200000L
                pr.updatedDate shouldBe 1704153600000L
            }
        }

        context("PullRequestRef") {
            test("deserializes with nested repository and @Json-mapped fields") {
                val json = """
                    {
                        "id": "refs/heads/main",
                        "displayId": "main",
                        "latestCommit": "abc123def",
                        "repository": {
                            "id": 100,
                            "slug": "my-repo",
                            "name": "My Repo"
                        }
                    }
                """.trimIndent()

                val ref = moshi.adapter(PullRequestRef::class.java).fromJson(json)!!

                ref.id shouldBe "refs/heads/main"
                ref.displayId shouldBe "main"
                ref.latestCommit shouldBe "abc123def"
                ref.repository?.slug shouldBe "my-repo"
            }
        }

        context("PullRequestParticipant") {
            test("deserializes participant with user") {
                val json = """
                    {
                        "user": {"name": "dev", "displayName": "Developer"},
                        "role": "REVIEWER",
                        "approved": true,
                        "status": "APPROVED"
                    }
                """.trimIndent()

                val participant = moshi.adapter(PullRequestParticipant::class.java).fromJson(json)!!

                participant.user?.name shouldBe "dev"
                participant.role shouldBe "REVIEWER"
                participant.approved shouldBe true
                participant.status shouldBe "APPROVED"
            }
        }

        context("PaginatedResponse") {
            test("deserializes with @Json-mapped pagination fields") {
                val json = """
                    {
                        "start": 0,
                        "limit": 25,
                        "size": 2,
                        "isLastPage": false,
                        "nextPageStart": 25,
                        "values": [
                            {"id": 1, "slug": "repo-a"},
                            {"id": 2, "slug": "repo-b"}
                        ]
                    }
                """.trimIndent()

                val type = Types.newParameterizedType(PaginatedResponse::class.java, Repository::class.java)
                val adapter = moshi.adapter<PaginatedResponse<Repository>>(type)
                val response = adapter.fromJson(json)!!

                response.start shouldBe 0
                response.limit shouldBe 25
                response.size shouldBe 2
                response.isLastPage shouldBe false
                response.nextPageStart shouldBe 25
                response.values shouldHaveSize 2
                response.values[0].slug shouldBe "repo-a"
            }

            test("defaults to empty list when values is absent") {
                val json = """{"start": 0, "isLastPage": true}"""

                val type = Types.newParameterizedType(PaginatedResponse::class.java, Repository::class.java)
                val adapter = moshi.adapter<PaginatedResponse<Repository>>(type)
                val response = adapter.fromJson(json)!!

                response.values shouldHaveSize 0
                response.isLastPage shouldBe true
                response.nextPageStart.shouldBeNull()
            }
        }
    }
}
