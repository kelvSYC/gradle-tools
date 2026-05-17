package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.gitea.MockGiteaBearerClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.Release
import com.kelvsyc.gradle.gitea.models.ReleaseAsset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.ResponseBody
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

class DownloadGiteaReleaseArtifactActionTest : FunSpec() {
    init {
        test("execute - downloads matching assets from release") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val release = Release(
                id = 1L,
                tagName = "v1.0.0",
                name = "Release 1.0.0",
                assets = listOf(
                    ReleaseAsset(
                        id = 1L,
                        name = "app-1.0.0.jar",
                        size = 5000L,
                        downloadCount = 10L,
                        browserDownloadUrl = "https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/1",
                    ),
                    ReleaseAsset(
                        id = 2L,
                        name = "app-1.0.0-sources.jar",
                        size = 3000L,
                        downloadCount = 5L,
                        browserDownloadUrl = "https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/2",
                    ),
                    ReleaseAsset(
                        id = 3L,
                        name = "app-1.0.0.md5",
                        size = 100L,
                        downloadCount = 10L,
                        browserDownloadUrl = "https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/3",
                    ),
                ),
            )

            val releaseCall = mockk<Call<Release>>()
            every { releaseCall.execute() } returns Response.success(release)
            every { client.getReleaseByTag("myowner", "myrepo", "v1.0.0") } returns releaseCall

            val jarBytes = "jar content".toByteArray()
            val md5Bytes = "abc123def456".toByteArray()

            val jarResponse = mockk<ResponseBody>()
            every { jarResponse.byteStream() } returns ByteArrayInputStream(jarBytes)
            val jarCall = mockk<Call<ResponseBody>>()
            every { jarCall.execute() } returns Response.success(jarResponse)

            val md5Response = mockk<ResponseBody>()
            every { md5Response.byteStream() } returns ByteArrayInputStream(md5Bytes)
            val md5Call = mockk<Call<ResponseBody>>()
            every { md5Call.execute() } returns Response.success(md5Response)

            every { client.downloadAsset("https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/1") } returns jarCall
            every { client.downloadAsset("https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/3") } returns md5Call

            val attrs = PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
            val tempPath = Files.createTempDirectory("test", attrs)
            val tempDir = tempPath.toFile()

            val params = project.objects.newInstance<DownloadGiteaReleaseArtifactAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.tag.set("v1.0.0")
            params.assetNames.set(listOf("app-1.0.0.jar", "app-1.0.0.md5"))
            params.outputDirectory.set(tempDir)

            val action = object : DownloadGiteaReleaseArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            val jarFile = tempDir.resolve("app-1.0.0.jar")
            val md5File = tempDir.resolve("app-1.0.0.md5")
            val sourcesFile = tempDir.resolve("app-1.0.0-sources.jar")

            jarFile.exists() shouldBe true
            jarFile.readBytes() shouldBe jarBytes
            md5File.exists() shouldBe true
            md5File.readBytes() shouldBe md5Bytes
            sourcesFile.exists() shouldBe false

            verify(exactly = 1) { client.downloadAsset("https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/1") }
            verify(exactly = 1) { client.downloadAsset("https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/3") }
            verify(exactly = 0) { client.downloadAsset("https://gitea.example.com/api/v1/repos/myowner/myrepo/releases/assets/2") }

            Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
        }

        test("execute - downloads single matching asset from release") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val release = Release(
                id = 1L,
                tagName = "v2.0.0",
                assets = listOf(
                    ReleaseAsset(
                        id = 1L,
                        name = "release-2.0.0.zip",
                        size = 10000L,
                        downloadCount = 50L,
                        browserDownloadUrl = "https://gitea.example.com/api/v1/repos/owner/repo/releases/assets/1",
                    ),
                ),
            )

            val releaseCall = mockk<Call<Release>>()
            every { releaseCall.execute() } returns Response.success(release)
            every { client.getReleaseByTag("owner", "repo", "v2.0.0") } returns releaseCall

            val zipBytes = "zip content here".toByteArray()
            val zipResponse = mockk<ResponseBody>()
            every { zipResponse.byteStream() } returns ByteArrayInputStream(zipBytes)
            val zipCall = mockk<Call<ResponseBody>>()
            every { zipCall.execute() } returns Response.success(zipResponse)

            every { client.downloadAsset("https://gitea.example.com/api/v1/repos/owner/repo/releases/assets/1") } returns zipCall

            val attrs = PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
            val tempPath = Files.createTempDirectory("test", attrs)
            val tempDir = tempPath.toFile()

            val params = project.objects.newInstance<DownloadGiteaReleaseArtifactAction.Parameters>()
            params.service.set(service)
            params.owner.set("owner")
            params.repo.set("repo")
            params.tag.set("v2.0.0")
            params.assetNames.set(listOf("release-2.0.0.zip"))
            params.outputDirectory.set(tempDir)

            val action = object : DownloadGiteaReleaseArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            val zipFile = tempDir.resolve("release-2.0.0.zip")
            zipFile.exists() shouldBe true
            zipFile.readBytes() shouldBe zipBytes

            Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
        }
    }
}


