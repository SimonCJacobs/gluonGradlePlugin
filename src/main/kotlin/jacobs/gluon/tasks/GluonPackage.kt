package jacobs.gluon.tasks

import com.gluonhq.substrate.Constants
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Assumes Mac output
 */
abstract class GluonPackage : GluonSubstrateTask() {

    @get:InputDirectory @Suppress("unused")
    val inputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
        targetTriplet().map { triplet ->
            project.layout.projectDirectory.dir("src").dir(triplet.os).dir(Constants.MACOS_ASSETS_FOLDER)
        }
    )

    @get:OutputDirectory @Suppress("unused")
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
        getTargetOutputDir().map { it.dir("$appName.app") }
    )

    @get:OutputFile @Suppress("unused")
    val outputFile: RegularFileProperty = project.objects.fileProperty().convention(
        getTargetOutputDir().map { it.file(getPackageFilename()) }
    )

    @get:Input
    abstract val vendorName: Property<String>

    private fun getTargetOutputDir(): Provider<Directory> {
        return gluonBuildDirectory().zip(targetTriplet()) { buildDir, triplet ->
            buildDir.dir(triplet.archOs)
        }
    }

    private fun getPackageFilename(): String {
        return "$appName.pkg"
    }

    /**
     * Unused for now as seems to require Apple developer membership to do properly
     */
  //  abstract val appleSigningIdentity: Property<String>

    @TaskAction
    fun createPackage() {
        val dispatcher = createDispatcher {
            bundleShortVersion = projectVersion
            bundleVersion = projectVersion
            description = projectDescription
            packageType = "pkg"
       //     providedSigningIdentity = appleSigningIdentity.get()
            isSkipSigning = true
            vendor = vendorName.get()
            version = projectVersion
            versionCode = projectVersion
        }
        dispatcher.nativePackage()
        // Fix strange naming in Gluon code
        getTargetOutputDir().get().let { targetOutputDir ->
            targetOutputDir.file("$appName-1.0.0.pkg")
                .asFile
                .renameTo(targetOutputDir.file(getPackageFilename()).asFile)
        }
    }

}