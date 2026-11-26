/**
 * The dependency license gate: `licensee` fails when a runtime dependency of the core has a license that is not
 * allowed below. Overrides for licenses that licensee cannot map from the POM (`allowUrl`) and for artifacts without
 * license metadata (`allowDependency`) live here, each with the verification as its reason.
 *
 * Applied by every core through `com.kelvsyc.internal.gradle-tools.core`.
 */

plugins {
    id("app.cash.licensee")
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allow("EPL-2.0")
    allow("CDDL-1.0")
    allow("CDDL-1.1")
    allow("Unicode-3.0")
    allow("Unicode-DFS-2016")
    allow("MIT-0")
    allow("UPL-1.0")

    allowUrl("https://aws.amazon.com/apache2.0") { because("Apache-2.0, verified by hand: the AWS SDK for Java v2 is Apache-2.0 (github.com/aws/aws-sdk-java-v2/blob/master/LICENSE.txt)") }
    allowUrl("https://github.com/googleapis/google-cloud-java/blob/main/LICENSE") { because("Apache-2.0, verified by hand: the repository LICENSE of google-cloud-java") }
    allowUrl("https://golang.org/LICENSE") { because("BSD-3-Clause, verified by hand: re2j carries the Go project's BSD-3-Clause license") }
    allowUrl("https://github.com/jopenlibs/vault-java-driver/blob/master/README.md#license") { because("MIT, verified by hand: the license section of the vault-java-driver README") }
    allowUrl("https://github.com/javaee/javax.annotation/blob/master/LICENSE") { because("CDDL-1.1, verified by hand: javax.annotation-api is dual-licensed, CDDL-1.1 elected (the other option is GPLv2 with Classpath Exception)") }
    allowUrl("http://opensource.org/licenses/UPL") { because("UPL-1.0, verified by hand: the URL is the OSI UPL-1.0 text; GraalVM SDK artifacts are UPL-1.0") }
    allowUrl("https://spdx.org/licenses/MIT.txt") { because("MIT, verified by hand: the URL is the SPDX MIT text") }
    allowUrl("https://opensource.org/license/mit") { because("MIT, verified by hand: the URL is the OSI MIT text") }
    allowUrl("https://github.com/apple/pkl/blob/main/LICENSE.txt") { because("Apache-2.0, verified by hand: the repository LICENSE of Pkl") }
    allowUrl("https://raw.githubusercontent.com/ThreeTen/threeten-extra/main/LICENSE.txt") { because("BSD-3-Clause, verified by hand: the repository LICENSE of threeten-extra") }
    allowUrl("https://raw.githubusercontent.com/ThreeTen/threetenbp/main/LICENSE.txt") { because("BSD-3-Clause, verified by hand: the repository LICENSE of threetenbp") }
    allowDependency("com.microsoft.azure", "msal4j", "1.26.0") { because("MIT, verified by hand: the POM names 'MIT License' without a URL") }
    allowDependency("com.microsoft.azure", "msal4j-persistence-extension", "1.3.0") { because("MIT, verified by hand: the POM names 'MIT License' without a URL") }
    allowDependency("com.github.spotbugs", "spotbugs-annotations", "4.9.8") { because("LGPL-2.1, an unbundled and unmodified transitive dependency of the AWS Secrets Manager caching client; allowed for this artifact only") }

    // Gradle supplies these at run time; they are not shipped.
    ignoreDependencies("org.jetbrains.kotlin", "kotlin-stdlib") { because("Supplied by Gradle") }
    ignoreDependencies("org.jetbrains.kotlin", "kotlin-stdlib-jdk7") { because("Supplied by Gradle") }
    ignoreDependencies("org.jetbrains.kotlin", "kotlin-stdlib-jdk8") { because("Supplied by Gradle") }
    ignoreDependencies("org.jetbrains.kotlin", "kotlin-reflect") { because("Supplied by Gradle") }
    ignoreDependencies("org.gradle.experimental") { because("The Gradle API is supplied by Gradle") }
    ignoreDependencies("com.kelvsyc.internal", "platform") { because("Internal platform BOM, stripped from publications") }
    ignoreDependencies("com.kelvsyc.kotlin") { because("first-party kotlin-tools libraries; their license is decided in that repository") }
}
