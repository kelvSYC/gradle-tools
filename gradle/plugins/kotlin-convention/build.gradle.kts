plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal.gradle-tools"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.conventions.platform))
    implementation(libs.detekt.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.gradlex.dependency.conflict.resolution.plugin)
    implementation(libs.gradlex.reproducible.builds.plugin)
}
