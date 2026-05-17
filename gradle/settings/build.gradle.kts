plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.kelvsyc.internal:semver-convention")
}
