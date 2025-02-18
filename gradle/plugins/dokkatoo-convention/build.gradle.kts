plugins {
    `kotlin-dsl`
}

group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    implementation(libs.dokkatoo.plugin)
}
