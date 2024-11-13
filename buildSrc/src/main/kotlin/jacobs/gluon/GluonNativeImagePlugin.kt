package jacobs.gluon

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import jacobs.gluon.tasks.GluonCompile
import jacobs.gluon.tasks.GluonLink
import jacobs.gluon.tasks.GluonRunTask
import jacobs.gluon.tasks.GluonSubstrateTask
import jacobs.gluon.tasks.GluonTask
import jacobs.gluon.tasks.GraalTracingAgent
import org.gradle.api.internal.tasks.JvmConstants

/**
 * As written, must be applied after Java/Kotlin JVM plugin
 */
class GluonNativeImagePlugin : Plugin<Project> {

    companion object {
        const val GLUON_BUILD_DIRECTORY = "gluon"
        const val GLUON_GROUP = "gluon"
    }

    private lateinit var configuration: GluonNativeImageConfiguration

    override fun apply(target: Project) {
        with(target) {
            configuration = extensions.create<GluonNativeImageConfiguration>("gluon").apply { initialise() }
            registerTasks()
        }
    }

    private fun Project.registerTasks() {
        val runGraalTracingAgent = registerGluonTask<GraalTracingAgent>("graalTracingAgent") {
            applicationArgs.set(configuration.tracingExtension.applicationArgs)
        }
        val gluonCompile = registerGluonSubstrateTask<GluonCompile>("gluonCompile", runGraalTracingAgent)
        val gluonLink = registerGluonSubstrateTask<GluonLink>("gluonLink", runGraalTracingAgent) {
            dependsOn(gluonCompile)
        }
        registerGluonTask<GluonRunTask>("gluonRun") {
            executableFile.set(gluonLink.flatMap { it.outputExecutableFile })
        }
    }

    private inline fun <reified T : GluonSubstrateTask> Project.registerGluonSubstrateTask(
        name: String, tracingAgentTask: TaskProvider<GraalTracingAgent>, crossinline configure: T.() -> Unit = {}
    ): TaskProvider<T> {
        return registerGluonTask(name) {
            graalCompilerArgs.set(configuration.graalCompilerArgs)
            target.set(configuration.targetPlatform)
            tracingDirectory.set(tracingAgentTask.flatMap { it.tracingBuildDirectory })
            configure()
        }
    }

    private inline fun <reified T : GluonTask> Project.registerGluonTask(
        name: String, crossinline configure: T.() -> Unit = {}
    ): TaskProvider<T> {
        return tasks.register<T>(name) {
            dependsOn(JvmConstants.CLASSES_TASK_NAME, JvmConstants.PROCESS_RESOURCES_TASK_NAME)
            pathToGluonJvm.value(configuration.gluonJvmDirectory)
            configure()
        }
    }

}