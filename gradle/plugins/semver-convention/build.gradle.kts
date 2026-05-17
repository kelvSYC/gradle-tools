plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.jgit)
}

gradlePlugin {
    plugins {
        create("semver") {
            id = "com.kelvsyc.internal.semver"
            implementationClass = "SemverSettingsPlugin"
        }
    }
}
