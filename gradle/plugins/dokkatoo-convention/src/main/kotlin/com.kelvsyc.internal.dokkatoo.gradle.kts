plugins {
    base
    id("dev.adamko.dokkatoo-html")
    id("dev.adamko.dokkatoo-javadoc")
}

dokkatoo {
    dokkatooSourceSets.configureEach {
        enableJdkDocumentationLink.set(true)
        enableKotlinStdLibDocumentationLink.set(true)

        externalDocumentationLinks.register("gradle") {
            url.set(uri("https://docs.gradle.org/current/kotlin-dsl/gradle"))
        }

        sourceLink {
            // FIXME replace 'main' with git commit ref
            remoteUrl(this@dokkatoo.modulePath.map { "https://github.com/kelvSYC/gradle-tools/blob/main/cores/$it" })
        }
    }
}

pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
        withJavadocJar()
    }

    tasks.named<Jar>("javadocJar") {
        from(tasks.dokkatooGeneratePublicationJavadoc)
    }
}

tasks.assemble {
    dependsOn(tasks.dokkatooGeneratePublicationHtml)
}
