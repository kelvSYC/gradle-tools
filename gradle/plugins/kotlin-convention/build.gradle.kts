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
    compileOnly(libs.kotlin.plugin)
    implementation(libs.gradlex.dependency.conflict.resolution.plugin)
    implementation(local.licensee.plugin)
}
