package jacobs.gluon

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create

abstract class GluonNativeImageConfiguration {

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
    abstract val targetPlatform: Property<GluonTarget>

    abstract class TracingConfiguration {
        abstract val applicationArgs: ListProperty<String>
    }

}