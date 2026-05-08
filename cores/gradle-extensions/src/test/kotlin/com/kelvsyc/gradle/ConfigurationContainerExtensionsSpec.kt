package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.tasks.SourceSet

class ConfigurationContainerExtensionsSpec : FunSpec() {
    init {
        test("annotationProcessor") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.annotationProcessorConfigurationName } returns name

            container.annotationProcessor(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("api") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.apiConfigurationName } returns name

            container.api(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("compileClasspath") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.compileClasspathConfigurationName } returns name

            container.compileClasspath(sourceSet)

            verify {
                container.named(name, ResolvableConfiguration::class.java)
            }
        }

        test("compileOnlyApi") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.compileOnlyApiConfigurationName } returns name

            container.compileOnlyApi(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("compileOnly") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.compileOnlyConfigurationName } returns name

            container.compileOnly(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("implementation") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.implementationConfigurationName } returns name

            container.implementation(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("javadocElements") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.javadocElementsConfigurationName } returns name

            container.javadocElements(sourceSet)

            verify {
                container.named(name, ConsumableConfiguration::class.java)
            }
        }

        test("runtimeClasspath") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.runtimeClasspathConfigurationName } returns name

            container.runtimeClasspath(sourceSet)

            verify {
                container.named(name, ResolvableConfiguration::class.java)
            }
        }

        test("runtimeElements") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.runtimeElementsConfigurationName } returns name

            container.runtimeElements(sourceSet)

            verify {
                container.named(name, ConsumableConfiguration::class.java)
            }
        }

        test("runtimeOnly") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.runtimeOnlyConfigurationName } returns name

            container.runtimeOnly(sourceSet)

            verify {
                container.named(name, DependencyScopeConfiguration::class.java)
            }
        }

        test("sourcesElements") {
            val container = mockk<ConfigurationContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.sourcesElementsConfigurationName } returns name

            container.sourcesElements(sourceSet)

            verify {
                container.named(name, ConsumableConfiguration::class.java)
            }
        }
    }
}
