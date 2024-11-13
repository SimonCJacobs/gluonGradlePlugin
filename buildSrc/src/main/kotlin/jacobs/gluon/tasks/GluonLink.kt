package jacobs.gluon.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GluonLink : GluonSubstrateTask() {

    @get:OutputFile
    val outputExecutableFile: RegularFileProperty = project.objects.fileProperty().convention(
        gluonBuildDirectory().zip(targetTriplet()) { buildDir, triplet ->
            buildDir.dir(triplet.archOs) // Set in com.gluonhq.substrate.SubstrateDispatcher constructor
                .file(appName)
        }
    )

    @TaskAction
    fun link() {
        createDispatcher().nativeLink()
    }

}