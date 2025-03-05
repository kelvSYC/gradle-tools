package com.kelvsyc.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

fun TaskContainer.classes(sourceSet: SourceSet) = named(sourceSet.classesTaskName)

inline fun <reified T : Task> TaskContainer.compile(sourceSet: SourceSet, language: String) =
    named<T>(sourceSet.getCompileTaskName(language))

fun TaskContainer.compileJava(sourceSet: SourceSet) = named<JavaCompile>(sourceSet.compileJavaTaskName)

fun TaskContainer.jar(sourceSet: SourceSet) = named<Jar>(sourceSet.jarTaskName)

fun TaskContainer.javadocJar(sourceSet: SourceSet) = named<Jar>(sourceSet.javadocJarTaskName)

inline fun <reified T : Copy> TaskContainer.processResources(sourceSet: SourceSet) =
    named<T>(sourceSet.processResourcesTaskName)

fun TaskContainer.sourcesJar(sourceSet: SourceSet) = named<Jar>(sourceSet.sourcesJarTaskName)
