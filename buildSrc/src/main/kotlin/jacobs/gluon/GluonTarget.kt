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
}