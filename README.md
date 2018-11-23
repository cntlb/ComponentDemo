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