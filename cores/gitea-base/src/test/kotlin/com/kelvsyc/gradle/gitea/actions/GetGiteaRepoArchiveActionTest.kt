package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.gitea.MockGiteaBearerClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.ResponseBody
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

class GetGiteaRepoArchiveActionTest : FunSpec() {
    init {
        test("execute - downloads tar.gz archive with correct filepath") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val filepathSlot = slot<String>()
            val testBytes = "test archive data".toByteArray()
            val responseBody = mockk<ResponseBody>()
            every { responseBody.byteStream() } returns ByteArrayInputStream(testBytes)

            val call = mockk<Call<ResponseBody>>()
            every { call.execute() } returns Response.success(responseBody)
            every {
                client.getArchive("myowner", "myrepo", capture(filepathSlot))
            } returns call

            val attrs = PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
            val tempPath = Files.createTempFile("test", ".tar.gz", attrs)
            val tempFile = tempPath.toFile()

            val params = project.objects.newInstance<GetGiteaRepoArchiveAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.ref.set("main")
            params.outputFile.set(tempFile)

            val action = object : GetGiteaRepoArchiveAction() {
                override fun getParameters() = params
            }
            action.execute()

            filepathSlot.captured shouldBe "main.tar.gz"
            tempFile.readBytes() shouldBe testBytes

            Files.delete(tempPath)
        }

        test("execute - infers zip format from file extension") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val filepathSlot = slot<String>()
            val testBytes = "test zip data".toByteArray()
            val responseBody = mockk<ResponseBody>()
            every { responseBody.byteStream() } returns ByteArrayInputStream(testBytes)

            val call = mockk<Call<ResponseBody>>()
            every { call.execute() } returns Response.success(responseBody)
            every {
                client.getArchive("myowner", "myrepo", capture(filepathSlot))
            } returns call

            val attrs = PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
            val tempPath = Files.createTempFile("test", ".zip", attrs)
            val tempFile = tempPath.toFile()

            val params = project.objects.newInstance<GetGiteaRepoArchiveAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.ref.set("v1.0.0")
            params.outputFile.set(tempFile)

            val action = object : GetGiteaRepoArchiveAction() {
                override fun getParameters() = params
            }
            action.execute()

            filepathSlot.captured shouldBe "v1.0.0.zip"
            tempFile.readBytes() shouldBe testBytes

            Files.delete(tempPath)
        }

        test("execute - infers tar.gz from .tgz extension") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val filepathSlot = slot<String>()
            val testBytes = "test tgz data".toByteArray()
            val responseBody = mockk<ResponseBody>()
            every { responseBody.byteStream() } returns ByteArrayInputStream(testBytes)

            val call = mockk<Call<ResponseBody>>()
            every { call.execute() } returns Response.success(responseBody)
            every {
                client.getArchive("myowner", "myrepo", capture(filepathSlot))
            } returns call

            val attrs = PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
            val tempPath = Files.createTempFile("test", ".tgz", attrs)
            val tempFile = tempPath.toFile()

            val params = project.objects.newInstance<GetGiteaRepoArchiveAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.ref.set("develop")
            params.outputFile.set(tempFile)

            val action = object : GetGiteaRepoArchiveAction() {
                override fun getParameters() = params
            }
            action.execute()

            filepathSlot.captured shouldBe "develop.tar.gz"
            tempFile.readBytes() shouldBe testBytes

            Files.delete(tempPath)
        }
    }
}


