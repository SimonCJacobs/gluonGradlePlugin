package jacobs.gluon.tasks

import com.gluonhq.substrate.Constants
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory

abstract class GluonCompile : GluonSubstrateTask() {

    @get:OutputDirectory @Suppress("unused")
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
        gluonBuildDirectory().zip(targetTriplet()) { buildDir, triplet ->
            buildDir.dir(triplet.archOs) // Set in com.gluonhq.substrate.SubstrateDispatcher constructor
                .dir(Constants.GVM_PATH)
        }
    )

    @TaskAction
    fun run() {
        createDispatcher().nativeCompile()
    }

}