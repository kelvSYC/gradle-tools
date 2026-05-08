package com.kelvsyc.gradle.providers

import com.kelvsyc.gradle.teamCity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class TeamCityProvidersSpec : FunSpec() {
    init {
        test("can be instantiated via ObjectFactory extension") {
            val project = ProjectBuilder.builder().build()

            val providers = project.objects.teamCity

            providers.shouldNotBeNull()
        }

        test("teamcityVersion provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.teamcityVersion.shouldNotBeNull()
        }

        test("buildNumber provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.buildNumber.shouldNotBeNull()
        }

        test("buildVcsNumber provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.buildVcsNumber.shouldNotBeNull()
        }

        test("isPersonal is absent when environment variable is not set") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("BUILD_IS_PERSONAL") == null) {
                providers.isPersonal.orNull.shouldBeNull()
            }
        }

        test("projectName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.projectName.shouldNotBeNull()
        }

        test("buildConfName provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.buildConfName.shouldNotBeNull()
        }

        test("buildPropertiesFilePath provider is present") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            providers.buildPropertiesFilePath.shouldNotBeNull()
        }

        test("buildId is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.buildId.orNull.shouldBeNull()
            }
        }

        test("buildTypeId is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.buildTypeId.orNull.shouldBeNull()
            }
        }

        test("serverUrl is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.serverUrl.orNull.shouldBeNull()
            }
        }

        test("agentName is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.agentName.orNull.shouldBeNull()
            }
        }

        test("checkoutDir is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.checkoutDir.orNull.shouldBeNull()
            }
        }

        test("workingDir is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.workingDir.orNull.shouldBeNull()
            }
        }

        test("tempDir is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.tempDir.orNull.shouldBeNull()
            }
        }

        test("branch is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.branch.orNull.shouldBeNull()
            }
        }

        test("isDefaultBranch is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.isDefaultBranch.orNull.shouldBeNull()
            }
        }

        test("agentHomeDir is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.agentHomeDir.orNull.shouldBeNull()
            }
        }

        test("agentToolsDir is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.agentToolsDir.orNull.shouldBeNull()
            }
        }

        test("configurationPropertiesFilePath is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.configurationPropertiesFilePath.orNull.shouldBeNull()
            }
        }

        test("vcsRootRevisions is absent when build properties file is not available") {
            val project = ProjectBuilder.builder().build()
            val providers = project.objects.teamCity

            if (System.getenv("TEAMCITY_BUILD_PROPERTIES_FILE") == null) {
                providers.vcsRootRevisions.orNull.shouldBeNull()
            }
        }
    }
}
