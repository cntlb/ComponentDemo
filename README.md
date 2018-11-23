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

