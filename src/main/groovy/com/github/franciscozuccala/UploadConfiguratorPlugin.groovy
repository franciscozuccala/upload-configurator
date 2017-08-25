package com.github.franciscozuccala

import com.github.franciscozuccala.extension.UploadConfiguratorPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.jvm.tasks.Jar

class UploadConfiguratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

//      Apply extension for configure plugin

        project.extensions.create("uploadConfigurator", UploadConfiguratorPluginExtension)

//      Apply maven plugin

        project.apply plugin: 'maven'

        project.task('androidSourcesJar', type: Jar) {
            classifier = 'sources'
            from project.android.sourceSets.main.java.sourceFiles, project.android.sourceSets.debug.java.sourceFiles
        }

        project.artifacts {
            archives project.tasks.androidSourcesJar
        }


        project.afterEvaluate {

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

//                      Generate the pom
                        println("Generating pom")

                        pom.project {
                            name project.uploadConfigurator.pomName
                            artifactId = project.uploadConfigurator.pomArtifactId
                            version = project.uploadConfigurator.pomVersion
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

//                      Write dependencies by hand to avoid errors
                        pom.withXml {
                            Node node = asNode()
                            def dependenciesNode = node.get('dependencies')
                            def dependenciesAdded = []
                            if (dependenciesNode == null || dependenciesNode.isEmpty()) {
                                dependenciesNode = node.appendNode('dependencies')
                                project.configurations.each { conf ->
                                    conf.allDependencies.each { dependency ->
                                        dependency.artifacts.each { art ->
                                            def dependencyName = "$dependency.group:$dependency.name:$dependency.version${(art.classifier != null) ? ":$art.classifier" : ""}"
                                            if (!dependenciesAdded.contains(dependencyName)) {
                                                dependenciesAdded.add(dependencyName)
                                                def dependencyNode = dependenciesNode.appendNode('dependency')
                                                dependencyNode.appendNode('groupId', dependency.group)
                                                dependencyNode.appendNode('artifactId', dependency.name)
                                                dependencyNode.appendNode('version', dependency.version)
                                                if (art.extension != null) {
                                                    dependencyNode.appendNode('type', art.extension)
                                                }
                                                if (art.classifier != null) {
                                                    dependencyNode.appendNode('classifier', art.classifier)
                                                }

                                                if (dependency instanceof ModuleDependency
                                                        && !dependency.getExcludeRules().isEmpty()) {
                                                    def exclusionsNode = dependencyNode.appendNode('exclusions')
                                                    dependency.getExcludeRules().each { rule ->
                                                        def exclusionNode = exclusionsNode.appendNode('exclusion')
                                                        if (rule.getGroup() != null) {
                                                            exclusionNode.appendNode('groupId', rule.getGroup())
                                                        }
                                                        if (rule.getModule() != null) {
                                                            exclusionNode.appendNode('artifactId', rule.getModule())
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
}

