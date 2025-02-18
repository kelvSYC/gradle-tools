plugins {
    base
    id("dev.adamko.dokkatoo-html")
    id("dev.adamko.dokkatoo-javadoc")
}

dokkatoo {

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
