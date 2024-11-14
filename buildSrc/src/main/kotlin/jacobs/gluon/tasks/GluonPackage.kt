package jacobs.gluon.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Unused for now as seems to require Apple developer membership to do properly.
 */
abstract class GluonPackage : GluonSubstrateTask() {

    @get:OutputDirectory @Suppress("unused") // Assumes a Mac output
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
        gluonBuildDirectory().zip(targetTriplet()) { buildDir, triplet ->
            buildDir.dir(triplet.archOs)
                .dir("$appName.app")
        }
    )

  //  abstract val appleSigningIdentity: Property<String>

    @TaskAction
    fun createPackage() {
        val dispatcher = createDispatcher {
            bundleShortVersion = projectVersion
            bundleVersion = projectVersion
       //     providedSigningIdentity = appleSigningIdentity.get()
            isSkipSigning = true
            version = projectVersion
            versionCode = projectVersion
        }
        dispatcher.nativePackage()
    }

}