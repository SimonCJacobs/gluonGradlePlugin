package jacobs.gluon.tasks

import org.gradle.api.tasks.TaskAction

abstract class GluonPackage : GluonSubstrateTask() {

    @TaskAction
    fun createPackage() {
        createDispatcher().nativePackage()
    }

}