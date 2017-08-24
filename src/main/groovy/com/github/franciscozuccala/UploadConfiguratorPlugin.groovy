package com.github.franciscozuccala

import com.github.franciscozuccala.extension.UploadConfiguratorPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class UploadConfiguratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

//      Apply extension for configure plugin

        project.extensions.create("uploadConfigurator", UploadConfiguratorPluginExtension)

//      Apply maven plugin

        project.apply plugin: 'maven'

        project.afterEvaluate {
            def variants = []

//          Generate artifacts for each variant

            project.android {
                try {
                    libraryVariants.all { variant ->
                        project.artifacts {
                            def variantSplited = ((String) variant.name).split(("(?=\\p{Upper})"))
                            def variantName = "${variantSplited[0].toString().toLowerCase()}-${variantSplited[1].toString().toLowerCase()}"
                            variants.add(variantName)

                            def fullVariantName = "${project.rootProject.name}-$variantName"

                            println("Generating artifact with name: ${fullVariantName}.aar")

                            archives file: project.file("build/outputs/aar/${fullVariantName}.aar"),
                                    name: "$fullVariantName-${project.version}", type: 'aar'
                        }
                    }
                } catch (Exception e) {
                    println("This plugin only works for libraries, not applications, the exception is $e.message")
                }
            }

            project.uploadArchives {
                repositories {
                    mavenDeployer {

//                      Define repositories for nexus

                        repository(url: project.uploadConfigurator.repository.url) {
                            println("Repository url: ${project.uploadConfigurator.repository.url}")
                            if (project.uploadConfigurator.repository.authentication != null) {
                                authentication(userName: project.uploadConfigurator.repository.authentication.userName,
                                        password: project.uploadConfigurator.repository.authentication.password)
                            }
                        }

                        snapshotRepository(url: project.uploadConfigurator.snapshotRepository.url) {
                            println("Snapshot Repository url: ${project.uploadConfigurator.snapshotRepository.url}")
                            if (project.uploadConfigurator.snapshotRepository.authentication != null) {
                                authentication(userName: project.uploadConfigurator.snapshotRepository.authentication.userName,
                                        password: project.uploadConfigurator.snapshotRepository.authentication.password)
                            }
                        }

//                      Generate poms for each artifact already generated
                        println("Generating poms for each variant")

                        variants.each {
//                          This allows to filter artifacts by variant name
                            addFilter(it) { artifact, file ->
                                artifact.name.contains(it)
                            }
//                          This generate pom with variant name, like decolar-debug
                            def actualPom = pom(it)
                            actualPom.artifactId = "${project.rootProject.name}-$it"
                            actualPom.version = project.version
                            actualPom.project {
                                name project.uploadConfigurator.pomName
                                packaging 'aar'
                                description project.uploadConfigurator.pomDescription
                                url project.uploadConfigurator.pomUrl
                                licenses {
                                    license {
                                        name 'The Apache Software License, Version 2.0'
                                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                    }
                                }
                                developers {
                                    project.uploadConfigurator.developers.each { dev ->
                                        developer {
                                            id dev.id
                                            name dev.name
                                            email dev.email
                                        }
                                    }
                                }
                            }

//                          Write dependencies by hand to avoid errors
                            actualPom.withXml {
                                def buildNode = asNode().appendNode('build')
                                buildNode.appendNode('finalName', "${project.rootProject.name}-${actualPom.version}.aar")
                                def dependenciesNode = asNode().appendNode('dependencies')
                                def dependenciesAdded = []
                                project.configurations.all { configuration ->
                                    configuration.allDependencies.each {
                                        if (it.group != null && it.name != null) {
                                            def dependency = "$it.group:$it.name:$it.version"
                                            if (!dependenciesAdded.contains(dependency)) {
                                                println("Writing dependency named: $it.group, $it.name, $it.version " +
                                                        "in configuration:  $configuration.name")
                                                dependenciesAdded.add(dependency)
                                                def dependencyNode = dependenciesNode.appendNode('dependency')
                                                dependencyNode.appendNode('groupId', it.group)
                                                dependencyNode.appendNode('artifactId', it.name)
                                                dependencyNode.appendNode('version', it.version)

                                                if (it.excludeRules.size() > 0) {
                                                    def exclusionsNode = dependencyNode.appendNode('exclusions')
                                                    it.excludeRules.each { rule ->
                                                        def exclusionNode = exclusionsNode.appendNode('exclusion')
                                                        exclusionNode.appendNode('groupId', rule.group)
                                                        exclusionNode.appendNode('artifactId', rule.module)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

