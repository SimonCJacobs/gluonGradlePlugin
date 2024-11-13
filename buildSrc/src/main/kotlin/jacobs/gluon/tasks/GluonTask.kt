package jacobs.gluon.tasks

import org.gradle.api.DefaultTask
import jacobs.gluon.GluonNativeImagePlugin
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory

abstract class GluonTask : DefaultTask() {

    init {
        group = GluonNativeImagePlugin.GLUON_GROUP
    }

    @get:InputDirectory
    abstract val pathToGluonJvm: DirectoryProperty

}