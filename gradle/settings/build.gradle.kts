plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.reckon.plugin)
}
