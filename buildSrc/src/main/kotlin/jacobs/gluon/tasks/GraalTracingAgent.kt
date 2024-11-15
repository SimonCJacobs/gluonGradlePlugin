package jacobs.gluon.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByName

abstract class GraalTracingAgent : GluonTask() {

    @get:Input @get:Optional
    abstract val applicationArgs: ListProperty<String>

    @get:OutputDirectory
    val tracingBuildDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("nativeImageTracingOutput"))

    private val nativeImageOutputDirString: String
        get() = tracingBuildDirectory.dir("META-INF/native-image/${project.name}").get().asFile.canonicalPath

    @TaskAction
    fun run() {
        with(project.tasks.getByName<JavaExec>(ApplicationPlugin.TASK_RUN_NAME)) {
            args(applicationArgs.get())
            runOnGluonJvm()
            setAsRunOfNativeImageAgent()
            completeJvmArguments()
            project.logger.info("Commencing tracing")
            exec()
        }
    }

    private fun JavaExec.runOnGluonJvm() {
        @Suppress("UsePropertyAccessSyntax") // doing that gives "can't set val" error
        setExecutable(pathToGluonJvm.get().dir("bin").file("java").asFile.canonicalPath)
    }

    private fun JavaExec.setAsRunOfNativeImageAgent() {
        val oldJvmArgs = jvmArguments.get()
        jvmArguments.set( // See https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/
            listOf("-agentlib:native-image-agent=config-output-dir=$nativeImageOutputDirString") + oldJvmArgs
        )
    }

    private fun JavaExec.completeJvmArguments() {
        assert(actions.size == 2) {
            "Expect \"run\" task to have two actions, the first having been added by the OpenJFX plugin v0.1.0. To fix if/when OpenJFX plugin updated"
        }
        actions.first().execute(this) // Add the JVM arguments from OpenJFX
    }

}