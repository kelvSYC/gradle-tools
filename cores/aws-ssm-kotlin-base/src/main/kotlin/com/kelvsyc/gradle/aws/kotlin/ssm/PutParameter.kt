package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.putParameter
import aws.sdk.kotlin.services.ssm.model.ParameterType
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that creates or updates a parameter in SSM Parameter Store.
 *
 * To update an existing parameter, set [overwrite] to `true`. The [parameterType] is only required when
 * creating a new parameter; for updates of an existing parameter the type may be omitted.
 */
@UntrackedTask(because = "Communicates with AWS SSM; no local output")
abstract class PutParameter : DefaultTask() {

    /** The build service managing the SSM client. */
    @get:Internal
    abstract val service: Property<SsmClientBuildService>

    /** The name of the parameter to create or update. */
    @get:Input
    abstract val parameterName: Property<String>

    /** The new value for the parameter. */
    @get:Input
    abstract val parameterValue: Property<String>

    /** The parameter type (one of `String`, `StringList`, `SecureString`); required when creating a parameter. */
    @get:Input
    abstract val parameterType: Property<String>

    /** Whether to overwrite an existing parameter with the same name. Defaults to `false`. */
    @get:Input
    @get:Optional
    abstract val overwrite: Property<Boolean>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().putParameter {
            name = parameterName.get()
            value = parameterValue.get()
            type = parameterType.orNull?.let { ParameterType.fromValue(it) }
            overwrite = this@PutParameter.overwrite.orNull
        }
    }
}
