
**首次同步工程失败请查看下面`应用插件`的注意事项**

## 构建脚本重构
抽取各个模块一些公共的配置到顶级build.gradle下，比如版本、依赖等
```groovy
buildscript{
    ext.versions = [
        compileSdk: 28,
        minSdk    : 15,
        targetSdk : 28,
    ]
}
```
然后到需要重构的模块/build.gradle中使用正则替换

![正则匹配替换重构公共代码](imgs/1.png)

勾选"Regex", 按需要勾选"Match Case"和"In Selection", `$1Version versions.$1`中的
`versions`部分来自`ext.versions`

依赖部分一样的方式重构.

最后顶级settings.gradle建议改用多行include的方式
```groovy
include ':app'
include ':component_video'
include ':component_music'
```
好处是可以选择多行进行注释, 编写Gradle插件时比较方便.

## 添加Gradle插件模块
新建一个java-library的模块**gradle_plugin**, 通过改`apply plugin: 'java'`为`apply plugin: 'groovy'`将
java library变成groovy工程. 加入gradle插件需要的依赖
```groovy
dependencies{
    implementation gradleApi()
    implementation localGroovy()
    //如果加了这个最好也重构到顶级构建脚本下
    implementation 'com.android.tools.build:gradle:3.2.1'
}
```

这里采用Groovy作为插件语言,将gradle_plugin/src/java重命名为groovy用来放置groovy代码
> groovy目录下当然也能添加java类, 建议按语言分类, 这种情况可以保留java目录新建groovy目录

## 插件模型

编写简单的插件模型发布到本地仓库**gradle_plugin/src/main/groovy/com/example/plugin/ComponentPlugin.groovy**
```groovy
package com.example.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComponentPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "ComponentPlugin apply for $project"
    }
}
```
新建插件映射文件**gradle_plugin/src/main/resources/META-INF/gradle-plugins/com.android.component.properties**
```properties
implementation-class=com.example.plugin.ComponentPlugin
```

添加uploadArchives任务,该任务发布插件到本地maven仓库,需要引入maven插件
**gradle_plugin/build.gradle**中添加
```
apply plugin: 'maven'

uploadArchives{
    repositories.mavenDeployer{
        repository(url:uri("../repo"))
        pom.project{
            groupId = 'com.example'
            artifactId = 'componentization'
            version = '1.0'
        }
    }
}
```
groupId,artifactId,version这三个可以自由定义, 最终使用时形如:
```
dependencies{
    classpath "$groupId:$artifactId:$version"
}
```
执行`./gradlew uploadArchives`后可以看到顶级目录下生成repo
```
repo/
└── com
    └── example
        └── componentization
            ├── 1.0
            │   ├── componentization-1.0.jar
            │   ├── componentization-1.0.jar.md5
            │   ├── componentization-1.0.jar.sha1
            │   ├── componentization-1.0.pom
            │   ├── componentization-1.0.pom.md5
            │   └── componentization-1.0.pom.sha1
            ├── maven-metadata.xml
            ├── maven-metadata.xml.md5
            └── maven-metadata.xml.sha1
```

## 应用插件
这时将项目切换到Android视图,点开"Gradle Scripts"比较方便. 顶级build.gradle添加
```groovy
buildscript {
    repositories {
        maven {
            url uri('repo')
        }
    }
    dependencies {
        classpath 'com.example:componentization:1.0'
    }
}
```
在`buildscript`块中添加本地仓库的路径就是当前根项目目录下的repo, 将插件的jar包加入到构建
classpath中, 选择一个模块来应用.

**app/build.gradle**添加`apply plugin: 'com.android.component'`后
执行`./gradlew :app:tasks`看到输出:
```shell
$ ./gradlew -q :app:tasks
ComponentPlugin apply for project ':app'

------------------------------------------------------------
All tasks runnable from project :app
------------------------------------------------------------

```
那么我们的插件就插件并应用成功.

**注意:** 这时如果将工程推送到github上别人clone下来使用, 由于没有将插件发布到中央
仓库而本地又没有生成插件jar包, 同步肯定会失败(删掉repo目录模拟这种情况). 同步都失败了
想执行`./gradlew uploadArchives`都跑不成功又怎么去生成本地jar包呢?
1. settings.gradle中只保留插件模块
    ```groovy
    //include ':app'
    //include ':component_video'
    //include ':component_music'
    include ':gradle_plugin'
    ```
2. 顶级gradle.build中注释`classpath 'com.example:componentization:1.0'`

这样将所有可能用到目前还不存在的插件都屏蔽了, 然后去生成jar包使用.

## 组件化项目
这里不详细讨论android的组件化, 查看以下文章以了解:
 * [Android彻底组件化方案实践](https://www.jianshu.com/p/1b1d77f58e84)
 * [浅谈Android组件化](https://link.jianshu.com/?t=https%3A%2F%2Fmp.weixin.qq.com%2Fs%2FRAOjrpie214w0byRndczmg)

实现组件化需要**代码解耦**和**独立运行**, 通过配置一个`isRunningAlone`来标记. 比如music组件
```groovy
if(isRunningAlone){
    apply plugin: 'com.android.application'
}else{
    apply plugin: 'com.android.library'
}
.....
resourcePrefix "music_"
sourceSets {
    main {
        manifest.srcFile 'src/runalone/AndroidManifest.xml'
        java.srcDirs += 'src/runalone/java'
        res.srcDirs += 'src/runalone/res'
        assets.srcDirs += 'src/runalone/assets'
        jniLibs.srcDirs += 'src/runalone/jniLibs'
    }
}
```
`runalone`目录的结构和`main`完全一样, 类似于多渠道, 作为组件独立运行的代码. 而`main`中的资源和代码则
作为library添加到其他组件的依赖中. 为了代码合并是尽量避免资源冲突,每个组件配置了专属`resourcePrefix`.

`resourcePrefix "music_"`允许资源命名为music_xxx, Music_Xxx, musicXxx, MusicXxx的形式

以下是插件的配置


| 配置属性                     | 说明                                       | 例子            |
| ------------------------ | ---------------------------------------- | ------------- |
| com.example.mainmodule   | 主模块名称(需要:作前缀), 配置在顶级gradle.properties中   | :app          |
| com.example.runningalone | 是否可以独立运行, 配置在各组件的gradle.properties中      | true          |
| com.example.dependencies | 独立运行时需要的依赖(依组件时需要`:`前缀), 依赖间通过`,`分割. 配置在各组件的gradle.properties中 | :music,:video |

