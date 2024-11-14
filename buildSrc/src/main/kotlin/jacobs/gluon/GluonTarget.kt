package jacobs.gluon

import com.gluonhq.substrate.Constants.Profile

enum class GluonTarget {
    MAC_INTEL {
        override val gluonProfile: Profile
            get() = Profile.MACOS
    },

    MAC_APPLE_SILICON {
        override val gluonProfile: Profile
            get() = Profile.MACOS_AARCH64
    };

    abstract val gluonProfile: Profile

    val nameCamelCase: String
        get() = nameTitleCase.replaceFirstChar { it.lowercase() }

    val nameTitleCase: String
        get() = name.split("_")
            .joinToString("") { chunk ->
                chunk.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
}