rootProject.name = "gradle-tools"

// Cores
file("cores").listFiles { dir -> dir.isDirectory && dir.resolve("settings.gradle.kts").exists() }?.forEach {
    includeBuild("cores/${it.name}")
}

includeBuild("aggregation")
includeBuild("metadata")
