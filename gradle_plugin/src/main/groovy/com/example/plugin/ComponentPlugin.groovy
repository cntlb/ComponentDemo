package com.example.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComponentPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "ComponentPlugin apply for $project"
    }
}