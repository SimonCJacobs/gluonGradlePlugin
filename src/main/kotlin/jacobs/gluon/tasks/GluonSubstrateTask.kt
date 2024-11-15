package jacobs.gluon.tasks

import com.gluonhq.substrate.ProjectConfiguration
import com.gluonhq.substrate.SubstrateDispatcher
import com.gluonhq.substrate.model.ReleaseConfiguration
import com.gluonhq.substrate.model.Triplet
import jacobs.gluon.GluonNativeImagePlugin
import jacobs.gluon.GluonTarget
import org.gradle.api.Project
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

    @get:Input
    abstract val target: Property<GluonTarget>

    @get:InputDirectory
    abstract val tracingDirectory: DirectoryProperty

    @get:Internal
    protected val appName: String
        get() = project.name

    @get:Internal
    protected val projectDescription: String
        get() = project.description ?: error("Must specify a project description")

    @get:Internal
    protected val projectVersion: String
        get() = if (project.version == Project.DEFAULT_VERSION)
                error("Must specify a project version")
            else
                project.version.toString()

    protected fun gluonBuildDirectory(): Provider<Directory> {
        return gluonDirectory.flatMap { gluonDir ->
            project.layout.buildDirectory.dir(gluonDir)
        }
    }

    protected fun targetTriplet(): Provider<Triplet> {
        return target.map { Triplet(it.gluonProfile) }
    }

    protected fun createDispatcher(releaseConfiguration: ReleaseConfiguration.() -> Unit = {}): SubstrateDispatcher {
        return SubstrateDispatcher(
            gluonBuildDirectory().get().asFile.toPath(),
            compileSubstrateConfig(releaseConfiguration)
        )
    }

    private fun compileSubstrateConfig(releaseConfiguration: ReleaseConfiguration.() -> Unit): ProjectConfiguration {
        val mainClassName = project.the<JavaApplication>().mainClass.get()
        val classpath = getClasspath()
        project.logger.info("Creating Gluon project using classpath $classpath")
        val substrateConfig = ProjectConfiguration(mainClassName, classpath)
        substrateConfig.appName = appName
        substrateConfig.compilerArgs = graalCompilerArgs.get()
        substrateConfig.graalPath = pathToGluonJvm.get().asFile.toPath()
        substrateConfig.setTarget(targetTriplet().get())
        substrateConfig.releaseConfiguration.apply(releaseConfiguration)
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