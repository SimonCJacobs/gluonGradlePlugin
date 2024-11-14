package jacobs.gluon

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.create

abstract class GluonNativeImageConfiguration(private val project: Project) {

    private val targetConfigurator: GluonTargetConfigurator by lazy { GluonTargetConfigurator(project, this) }

    internal fun initialise() {
        tracingExtension = (this as ExtensionAware).extensions.create<TracingConfiguration>("tracing")
    }

    internal lateinit var tracingExtension: TracingConfiguration

    abstract val gluonJvmDirectory: DirectoryProperty
    /**
     * Additional arguments passed to the Graal compiler
     *
     * https://www.graalvm.org/latest/reference-manual/native-image/overview/Options/
     */
    abstract val graalCompilerArgs: ListProperty<String>

    fun targetPlatforms(vararg targets: GluonTarget) {
        targets.forEach { eachTarget ->
            targetConfigurator.addTarget(eachTarget)
        }
    }

    abstract class TracingConfiguration {
        abstract val applicationArgs: ListProperty<String>
    }

}