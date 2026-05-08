@file:Suppress("detekt:TooManyFunctions")

package com.kelvsyc.gradle

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.tasks.SourceSet

/** Returns the annotation processor [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.annotationProcessor(sourceSet: SourceSet) =
    named(sourceSet.annotationProcessorConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the `api` [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.api(sourceSet: SourceSet) =
    named(sourceSet.apiConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the compile classpath [ResolvableConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.compileClasspath(sourceSet: SourceSet) =
    named(sourceSet.compileClasspathConfigurationName, ResolvableConfiguration::class.java)

/** Returns the `compileOnlyApi` [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.compileOnlyApi(sourceSet: SourceSet) =
    named(sourceSet.compileOnlyApiConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the `compileOnly` [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.compileOnly(sourceSet: SourceSet) =
    named(sourceSet.compileOnlyConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the `implementation` [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.implementation(sourceSet: SourceSet) =
    named(sourceSet.implementationConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the Javadoc elements [ConsumableConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.javadocElements(sourceSet: SourceSet) =
    named(sourceSet.javadocElementsConfigurationName, ConsumableConfiguration::class.java)

/** Returns the runtime classpath [ResolvableConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.runtimeClasspath(sourceSet: SourceSet) =
    named(sourceSet.runtimeClasspathConfigurationName, ResolvableConfiguration::class.java)

/** Returns the runtime elements [ConsumableConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.runtimeElements(sourceSet: SourceSet) =
    named(sourceSet.runtimeElementsConfigurationName, ConsumableConfiguration::class.java)

/** Returns the `runtimeOnly` [DependencyScopeConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.runtimeOnly(sourceSet: SourceSet) =
    named(sourceSet.runtimeOnlyConfigurationName, DependencyScopeConfiguration::class.java)

/** Returns the sources elements [ConsumableConfiguration] for the given [sourceSet]. */
fun ConfigurationContainer.sourcesElements(sourceSet: SourceSet) =
    named(sourceSet.sourcesElementsConfigurationName, ConsumableConfiguration::class.java)
