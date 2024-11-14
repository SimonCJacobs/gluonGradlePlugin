package jacobs.gluon.tasks

import jacobs.gluon.GluonNativeImageConfiguration
import org.gradle.api.Project
import org.gradle.api.internal.tasks.JvmConstants
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

inline fun <reified T : GluonTask> Project.registerGluonTask(
    name: String,
    configuration: GluonNativeImageConfiguration,
    crossinline configure: T.() -> Unit = {}
): TaskProvider<T> {
    return tasks.register<T>(name) {
        dependsOn(JvmConstants.CLASSES_TASK_NAME, JvmConstants.PROCESS_RESOURCES_TASK_NAME)
        pathToGluonJvm.value(configuration.gluonJvmDirectory)
        configure()
    }
}