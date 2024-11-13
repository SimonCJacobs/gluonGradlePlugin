plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gluonSubstrate)
}

gradlePlugin {
    plugins {
        create("gluonNativeImagePlugin") {
            id = "gluon-native-image"
            implementationClass = "jacobs.gluon.GluonNativeImagePlugin"
        }
    }
}