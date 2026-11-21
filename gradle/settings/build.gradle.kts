plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal.gradle-tools"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.conventions.settings.plugin)
}
