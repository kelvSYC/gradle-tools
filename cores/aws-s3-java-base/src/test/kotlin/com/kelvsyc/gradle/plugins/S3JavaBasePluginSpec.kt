package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.s3.S3AsyncClientInfo
import com.kelvsyc.gradle.aws.java.s3.S3ClientInfo
import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class S3JavaBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers client info type") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(S3JavaBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain S3ClientInfo::class.java
            clients.createableTypes shouldContain S3AsyncClientInfo::class.java
            clients.createableTypes shouldContain S3TransferManagerClientInfo::class.java
        }
    }
}
