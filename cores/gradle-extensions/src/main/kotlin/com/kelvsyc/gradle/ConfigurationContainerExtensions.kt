@file:Suppress("detekt:TooManyFunctions")

package com.kelvsyc.gradle

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.tasks.SourceSet

fun ConfigurationContainer.annotationProcessor(sourceSet: SourceSet) =
    named(sourceSet.annotationProcessorConfigurationName)

fun ConfigurationContainer.api(sourceSet: SourceSet) = named(sourceSet.apiConfigurationName)

fun ConfigurationContainer.compileClasspath(sourceSet: SourceSet) = named(sourceSet.compileClasspathConfigurationName)

fun ConfigurationContainer.compileOnlyApi(sourceSet: SourceSet) = named(sourceSet.compileOnlyApiConfigurationName)

fun ConfigurationContainer.compileOnly(sourceSet: SourceSet) = named(sourceSet.compileOnlyConfigurationName)

fun ConfigurationContainer.implementation(sourceSet: SourceSet) = named(sourceSet.implementationConfigurationName)

fun ConfigurationContainer.javadocElements(sourceSet: SourceSet) = named(sourceSet.javadocElementsConfigurationName)

fun ConfigurationContainer.runtimeClasspath(sourceSet: SourceSet) = named(sourceSet.runtimeClasspathConfigurationName)

fun ConfigurationContainer.runtimeElements(sourceSet: SourceSet) = named(sourceSet.runtimeElementsConfigurationName)

fun ConfigurationContainer.runtimeOnly(sourceSet: SourceSet) = named(sourceSet.runtimeOnlyConfigurationName)

fun ConfigurationContainer.sourcesElements(sourceSet: SourceSet) = named(sourceSet.sourcesElementsConfigurationName)
