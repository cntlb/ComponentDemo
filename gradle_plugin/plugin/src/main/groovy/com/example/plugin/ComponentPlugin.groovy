package com.example.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Android组件化Gradle插件
 */
class ComponentPlugin implements Plugin<Project> {
    private static final String prefix = 'com.example'
    // 主模块名称(需要:作前缀), 配置在顶级gradle.properties中
    private static final String KEY_MAIN_MODULE = "${prefix}.mainmodule"
    // 是否可以独立运行, 配置在各组件的gradle.properties中
    private static final String KEY_RUNNING_ALONE = "${prefix}.runningalone"
    // 独立运行时需要的依赖(依组件时需要:前缀). 配置在各组件的gradle.properties中
    private static final String KEY_DEPENDENCIES = "${prefix}.dependencies"
    // 默认主模块 :app
    private static final String DEFAULT_MAIN_MODULE = ':app'

    @Override
    void apply(Project project) {
        // 模块没有指定$KEY_MAIN_MODULE直接退出, 不影响模块
        if (!project.hasProperty(KEY_RUNNING_ALONE)) {
            return
        }

        // 解析构建参数, 获取构建信息
        BuildInfo info = genBuildInfo(project)

        // 查找主模块
        def mainModule
        def currentModule = project.path
        if (project.rootProject.hasProperty(KEY_MAIN_MODULE)) {
            mainModule = project.rootProject.property(KEY_MAIN_MODULE)
        } else {
            Log.e "没有在${project.rootProject.name}/gradle.properties中配置${KEY_MAIN_MODULE}" +
                    "将使用${DEFAULT_MAIN_MODULE}作为默认主模块"
            mainModule = DEFAULT_MAIN_MODULE
        }
        info.modules += mainModule

        // 判断当前模块是否支持独立运行
        boolean isRunningAlone = Boolean.parseBoolean(project.property(KEY_RUNNING_ALONE))
        if (isRunningAlone && info.isAssemble) {
            // 编译任务而当前模块又不在涉及的模块列表中, 说明当前模块是其他模块的依赖, 不能让它独立运行
            isRunningAlone = currentModule in info.modules
        }
        project.setProperty(KEY_RUNNING_ALONE, isRunningAlone)

        if (isRunningAlone) {
            // 独立运行就是application, 应用该插件
            project.apply plugin: 'com.android.application'
            Log.i "$project apply plugin: 'com.android.application'"
            if (currentModule != mainModule) {
                // 修改作为一个app的项目结构
                project.android.sourceSets {
                    main {
                        manifest.srcFile 'src/runalone/AndroidManifest.xml'
                        java.srcDirs += 'src/runalone/java'
                        res.srcDirs += 'src/runalone/res'
                        assets.srcDirs += 'src/runalone/assets'
                        jniLibs.srcDirs += 'src/runalone/jniLibs'
                    }
                }
            }

            // 配置工程的依赖
            // 1. 自己不能依赖自己
            // 2. 只在独立运行工程的编译期添加其他组件(完全隔离了)作为依赖
            if (info.isAssemble && project.hasProperty(KEY_DEPENDENCIES)) {
                def dependencyList = ((String) project.property(KEY_DEPENDENCIES)).split('\\s*,\\s*')
                Log.i 'additional dependencies {'
                for (def aDependency : dependencyList) {
                    // 不能依赖自己和主模块
                    if (currentModule == aDependency) {
                        Log.e "${project}依赖自己($currentModule)将被忽略"
                        continue
                    }

                    def depend = aDependency
                    if (aDependency.startsWith(':'))
                        depend = project.project(aDependency)
                    Log.i "   implementation $depend"
                    project.dependencies.add('implementation', depend)
                }
                Log.i '}'
            }

            //class转换
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(new LifecycleTransform())
        } else {
            project.apply plugin: 'com.android.library'
            Log.i "$project apply plugin: 'com.android.library'"
        }
    }


    private static BuildInfo genBuildInfo(Project project) {
        BuildInfo info = new BuildInfo()
        // 这里的taskNames是执行gradle命令时传递就来的
        // 同步则执行->  ./gradlew :app:generateDebugSources :component_video:generateDebugSources ...
        // 运行app->    ./gradlew :app:assemble
        for (def taskName : project.gradle.startParameter.taskNames) {
            // :app:generateDebugSources -> [:app, generateDebugSources]
            // assemble                  -> [assemble]
            def split = taskName.split("(?!^):")
            if (split.length <= 0) continue
            def task
            if (split.length == 1) {
                task = split[0]
            } else {
                info.modules.add(split[0])
                task = split[1]
            }

            if (task.toLowerCase().contains('assemble'))
                info.isAssemble = true
        }
        info
    }

    /**
     * 构建信息
     */
    static class BuildInfo {
        /**
         * 当前gradle任务执行时涉及到的模块
         */
        Set<String> modules = new HashSet<>()
        /**
         * 是否为编译任务, 像点击运行按钮这样的属于编译任务, 同步就不属于编译
         * 可以点开gradle任务查看
         */
        boolean isAssemble
    }

    /**
     * 日志类
     */
    static class Log {
        static final String TAG = "${prefix}.gradle>>>>>>>> "

        static void i(Object value) {
            println "$TAG$value"
        }

        static void e(Object value) {
            System.err.println "$TAG$value"
        }
    }
}