package jacobs.gluon.tasks

import com.gluonhq.substrate.ProjectConfiguration
import com.gluonhq.substrate.SubstrateDispatcher
import com.gluonhq.substrate.model.Triplet
import jacobs.gluon.GluonNativeImagePlugin
import jacobs.gluon.GluonTarget
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.tasks.JvmConstants
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the
import java.io.File

abstract class GluonSubstrateTask : GluonTask() {

    @get:Input
    val gluonDirectory: Property<String> = project.objects.property<String>().convention(
        GluonNativeImagePlugin.GLUON_BUILD_DIRECTORY
    )

    @get:Input
    abstract val graalCompilerArgs: ListProperty<String>

    @get:Input @get:Optional
    abstract val target: Property<GluonTarget?>

    @get:InputDirectory
    abstract val tracingDirectory: DirectoryProperty

    @get:Internal
    protected val appName: String
        get() = project.name

    protected fun gluonBuildDirectory(): Provider<Directory> {
        return gluonDirectory.flatMap { gluonDir ->
            project.layout.buildDirectory.dir(gluonDir)
        }
    }

    protected fun targetTriplet(): Provider<Triplet> {
        return target.map { t -> t?.let { Triplet(it.gluonProfile) } ?: Triplet.fromCurrentOS() }
    }

    protected fun createDispatcher(): SubstrateDispatcher {
        return SubstrateDispatcher(gluonBuildDirectory().get().asFile.toPath(), compileSubstrateConfig())
    }

    private fun compileSubstrateConfig(): ProjectConfiguration {
        val mainClassName = project.the<JavaApplication>().mainClass.get()
        val substrateConfig = ProjectConfiguration(mainClassName, getClasspath())
        substrateConfig.appName = appName
        substrateConfig.compilerArgs = graalCompilerArgs.get()
        substrateConfig.graalPath = pathToGluonJvm.get().asFile.toPath()
        substrateConfig.setTarget(targetTriplet().get())
        return substrateConfig
    }

    private fun getClasspath(): String {
        return project.the<SourceSetContainer>()
            .getByName(JvmConstants.JAVA_MAIN_FEATURE_NAME)
            .runtimeClasspath
            .filter { eachFile -> eachFile.exists() } // Otherwise Gluon throws error
            .asPath
            .addTracingResourcesDirectory()
    }

    private fun String.addTracingResourcesDirectory(): String {
        return this + File.pathSeparator + tracingDirectory.asFile.get().canonicalPath
    }

}