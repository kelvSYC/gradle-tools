plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    implementation(libs.dependency.analysis.plugin)
    implementation(libs.detekt.plugin)
    implementation(libs.gradlex.dependency.conflict.resolution.plugin)
    implementation(libs.gradlex.reproducible.builds.plugin)
    implementation(libs.kotlin.dsl.plugins)
    implementation(libs.kotlin.plugin)
}
