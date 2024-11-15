package jacobs.gluon

import jacobs.gluon.tasks.GluonCompile
import jacobs.gluon.tasks.GluonLink
import jacobs.gluon.tasks.GluonPackage
import jacobs.gluon.tasks.GluonRunTask
import jacobs.gluon.tasks.GluonSubstrateTask
import jacobs.gluon.tasks.registerGluonTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class GluonTargetConfigurator(
    private val project: Project,
    private val configuration: GluonNativeImageConfiguration
) {

    private val targetContainer = project.objects.domainObjectContainer(GluonTargetObject::class.java)
    private val tracingAgentTask get() = GluonNativeImagePlugin.tracingAgentTask

    init {
        targetContainer.all { configureTarget(this) }
    }

    fun addTarget(target: GluonTarget) {
        targetContainer.add(GluonTargetObject(target))
    }

    private fun configureTarget(targetObject: GluonTargetObject) {
        with(project) {
            val gluonCompile = registerGluonSubstrateTask<GluonCompile>("gluonCompile", targetObject)
            val gluonLink = registerGluonSubstrateTask<GluonLink>("gluonLink", targetObject) {
                dependsOn(gluonCompile)
            }
            registerGluonSubstrateTask<GluonPackage>("gluonPackage", targetObject) {
                vendorName.set(configuration.releaseExtension.vendorName)
                dependsOn(gluonLink)
            }
            registerGluonTask<GluonRunTask>(targetTaskName("gluonRun", targetObject), configuration) {
                executableFile.set(gluonLink.flatMap { it.outputExecutableFile })
            }
        }
    }

    private inline fun <reified T : GluonSubstrateTask> Project.registerGluonSubstrateTask(
        baseName: String,
        targetObject: GluonTargetObject,
        crossinline configure: T.() -> Unit = {}
    ): TaskProvider<T> {
        return registerGluonTask(targetTaskName(baseName, targetObject), configuration) {
            graalCompilerArgs.set(configuration.graalCompilerArgs)
            target.set(targetObject.target)
            tracingDirectory.set(tracingAgentTask.flatMap { it.tracingBuildDirectory })
            configure()
        }
    }

    private fun targetTaskName(baseName: String, targetObject: GluonTargetObject): String {
        return baseName + targetObject.target.nameTitleCase
    }

    class GluonTargetObject(
        val target: GluonTarget
    ) {
        @Suppress("unused") // Needed for NamedDomainObjectContainer
        val name: String get() = target.nameCamelCase
    }

}