plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.reckon.plugin)
}
