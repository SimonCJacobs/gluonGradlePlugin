package jacobs.gluon

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import jacobs.gluon.tasks.GraalTracingAgent
import jacobs.gluon.tasks.registerGluonTask

/**
 * As written, must be applied after Java/Kotlin JVM plugin
 */
class GluonNativeImagePlugin : Plugin<Project> {

    companion object {
        const val GLUON_BUILD_DIRECTORY = "gluon"
        const val GLUON_GROUP = "gluon"

        lateinit var tracingAgentTask: TaskProvider<GraalTracingAgent>
    }

    private lateinit var configuration: GluonNativeImageConfiguration

    override fun apply(target: Project) {
        with(target) {
            configuration = extensions.create<GluonNativeImageConfiguration>("gluon", target)
                .apply { initialise() }
            tracingAgentTask = registerGluonTask<GraalTracingAgent>("runGraalTracingAgent", configuration) {
                applicationArgs.set(configuration.tracingExtension.applicationArgs)
            }
        }
    }

}