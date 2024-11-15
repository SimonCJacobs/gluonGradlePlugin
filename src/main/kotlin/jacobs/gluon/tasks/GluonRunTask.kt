package jacobs.gluon.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class GluonRunTask : GluonTask() {

    @get:InputFile
    abstract val executableFile: RegularFileProperty

    @TaskAction
    fun run() {
        project.exec {
            commandLine(executableFile.get().asFile.canonicalPath)
        }
    }

}