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
            url("https://docs.gradle.org/current/kotlin-dsl/gradle")
        }
        externalDocumentationLinks.register("commons-lang") {
            url("https://commons.apache.org/proper/commons-lang/apidocs")
        }
        externalDocumentationLinks.register("commons-numbers") {
            url("https://commons.apache.org/proper/commons-numbers/commons-numbers-docs/apidocs")
        }
        externalDocumentationLinks.register("guava") {
            url("https://javadoc.io/doc/com.google.guava/guava/latest")
            packageListUrl("https://javadoc.io/doc/com.google.guava/guava/latest/element-list")
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
