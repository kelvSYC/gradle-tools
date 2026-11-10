package com.kelvsyc.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

fun TaskContainer.classes(sourceSet: SourceSet) = named(sourceSet.classesTaskName)

inline fun <reified T : Task> TaskContainer.compile(sourceSet: SourceSet, language: String) =
    named(sourceSet.getCompileTaskName(language), T::class.java)

fun TaskContainer.compileJava(sourceSet: SourceSet) = named(sourceSet.compileJavaTaskName, JavaCompile::class.java)

fun TaskContainer.jar(sourceSet: SourceSet) = named(sourceSet.jarTaskName, Jar::class.java)

fun TaskContainer.javadocJar(sourceSet: SourceSet) = named(sourceSet.javadocJarTaskName, Jar::class.java)

inline fun <reified T : Copy> TaskContainer.processResources(sourceSet: SourceSet) =
    named(sourceSet.processResourcesTaskName, T::class.java)

fun TaskContainer.sourcesJar(sourceSet: SourceSet) = named(sourceSet.sourcesJarTaskName, Jar::class.java)
