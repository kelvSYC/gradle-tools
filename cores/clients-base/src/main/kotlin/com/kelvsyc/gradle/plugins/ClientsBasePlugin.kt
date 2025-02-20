package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.registerIfAbsent

class ClientsBasePlugin : Plugin<PluginAware> {
    companion object {
        const val SERVICE_NAME = "serviceClients"
        const val EXTENSION_NAME = "serviceClients"
    }

    override fun apply(target: PluginAware) {
        val gradle = when (target) {
            is Project -> target.gradle
            is Settings -> target.gradle
            is Gradle -> target
            else -> error("Unknown PluginAware type ${target.javaClass.name}")
        }

        val service = gradle.sharedServices.registerIfAbsent(SERVICE_NAME, ClientsBaseService::class)
        when (target) {
            is Project -> {
                // This plugin may have been applied as an init or settings plugin already, so the extension might
                // already have been created.
                val existingExtensionClassName = target.extensions.extensionsSchema.elements
                    .find { it.name == EXTENSION_NAME }?.publicType?.fullyQualifiedName
                if (existingExtensionClassName == null) {
                    target.extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                } else if (existingExtensionClassName != ClientsBaseExtension::class.qualifiedName) {
                    throw GradleException(
                        "Extension '$EXTENSION_NAME' already registered of type '$existingExtensionClassName'")
                }
            }
            is Settings -> {
                // This plugin may have been applied as an init plugin, so the extension might already have been created
                val existingExtensionClassName = target.extensions.extensionsSchema.elements
                    .find { it.name == EXTENSION_NAME }?.publicType?.fullyQualifiedName
                if (existingExtensionClassName == null) {
                    target.extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                } else if (existingExtensionClassName != ClientsBaseExtension::class.qualifiedName) {
                    throw GradleException(
                        "Extension '$EXTENSION_NAME' already registered of type '$existingExtensionClassName'")
                }
                // Apply to all projects
                gradle.allprojects {
                    extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                }
            }
            is Gradle -> {
                target.extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                gradle.beforeSettings {
                    extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                }
                gradle.allprojects {
                    extensions.create<ClientsBaseExtension>(EXTENSION_NAME, service)
                }
            }
        }
    }
}
