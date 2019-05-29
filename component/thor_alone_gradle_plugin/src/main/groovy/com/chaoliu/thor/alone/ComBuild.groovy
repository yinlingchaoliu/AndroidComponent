package com.chaoliu.thor.alone

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * 根据得到app启发,参考CC插件写法
 *
 * 1、将app moudle 单独调试和发布
 * 2、壳工程app 集成调试 避免依赖子module内容 做到代码隔离
 *
 * 对原有代码逻辑进行优化，尽量减少配置和歧义
 * 壳工程用isMainApp指定
 * 组件工程采用isRunAlone指定
 * 插件也需要单一职责 一个插件只做一类事
 * 插件意义在于减少gradle不必要配置
 *
 * @author:chentong
 * @date: 2019-5-9
 *
 */
class ComBuild implements Plugin<Project> {

    private static final String DEBUG_DIR = "src/main/debug/"
    private static final String IS_MAIN_APP = "isMainApp"
    private static final String IS_RUN_ALONE = "isRunAlone"

    private boolean isDebug = true
    private Project mProject
    private AssembleTask assembleTask = new AssembleTask()

    void apply(Project project) {
        this.mProject = project

        //初始化当前项目
        initAlone(project)

        //判断当前任务 只有assemble任务才添加依赖
        assembleTask = getTaskInfo(project)

        //当前是壳工程 不许被引用
        if (isMainApp(project)) {
            //运行壳工程配置
            runMainApp(project)
            //添加配置依赖
            compileComponents(assembleTask, project)
            //添加扩展方法 (选项) 参数
            project.ext.addComponent = { dependencyName -> addComponent(dependencyName) }
            return
        }

        //支持单独调试和发布
        runAlone(project)

    }

    /**
     * 对当前task初始化
     * @param project
     * @return
     */
    private AssembleTask getTaskInfo(Project project) {
        List<String> taskNames = project.gradle.startParameter.taskNames
        AssembleTask assembleTask = new AssembleTask()
        for (String task : taskNames) {
            if (task.toUpperCase().contains("ASSEMBLE")
                    || task.toUpperCase().contains("AR")
                    || task.toUpperCase().contains("ASR")
                    || task.toUpperCase().contains("ASD")
                    || task.toUpperCase().contains("TINKER")
                    || task.toUpperCase().contains("INSTALL")
                    || task.toUpperCase().contains("RESGUARD")) {
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true
                }
                assembleTask.isAssemble = true
                break
            }
        }
        return assembleTask
    }

    /**
     * 初始化必要状态
     * @param project
     * @param configName
     */
    private void initAlone(Project project) {
        //1.查找当前module下，是否存在该配置
        if (!project.hasProperty(IS_RUN_ALONE)) {
            //2.若不存在，则往root.project查找
            if (!project.rootProject.hasProperty(IS_RUN_ALONE)) {
                //若依然不存在 则设置isRunAlone 为false
                project.rootProject.setProperty(IS_RUN_ALONE, false)
            }
        }
    }

    /**
     * 运行壳工程
     */
    private void runMainApp(Project project) {
        project.apply plugin: 'com.android.application'
        log("apply plugin is shell app" + 'com.android.application')
    }

    /**
     * 支持单独调试和发布
     */
    private void runAlone(Project project) {
        //1.找当前module的状态
        boolean isRunAlone = fetchAloneStatus(project)
        if (isRunAlone) {
            project.apply plugin: 'com.android.application'
            log("apply plugin is " + 'com.android.application')
            project.android.sourceSets {
                main {
                    //debug模式下，如果存在src/main/debug/AndroidManifest.xml，则自动使用其作为manifest文件
                    def debugManifest = "${DEBUG_DIR}AndroidManifest.xml"
                    if (project.file(debugManifest).exists()) {
                        manifest.srcFile debugManifest
                    }
                    java.srcDirs = ['src/main/java', "${DEBUG_DIR}java"]
                    res.srcDirs = ['src/main/res', "${DEBUG_DIR}res"]
                    assets.srcDirs = ['src/main/assets', "${DEBUG_DIR}assets"]
                    jniLibs.srcDirs = ['src/main/jniLibs', "${DEBUG_DIR}jniLibs"]
                }
            }
        } else {
            project.apply plugin: 'com.android.library'
            log("apply plugin is " + 'com.android.library')
            //main下删除所有debug目录下的文件
            project.android.sourceSets.main {
                //默认这个位置，为代码清晰
                manifest.srcFile 'src/main/AndroidManifest.xml'
                //删除所有debug目录下内容
                java {
                    exclude 'debug/**'
                }
                res {
                    exclude 'debug/**'
                }
                assets {
                    exclude 'debug/**'
                }
                jniLibs {
                    exclude 'debug/**'
                }
            }
        }
    }

    /**
     * 查找isRunAlone 设置的值
     * @param project
     * @return
     */
    private boolean fetchAloneStatus(Project project) {
        //1.优先查找当前module下，并以之为准
        if (project.hasProperty(IS_RUN_ALONE)) {
            return Boolean.parseBoolean((project.properties.get(IS_RUN_ALONE)))
        }
        //2.查找rootProject
        if (project.rootProject.hasProperty(IS_RUN_ALONE)) {
            return Boolean.parseBoolean((project.rootProject.properties.get(IS_RUN_ALONE)))
        }

        //3.查找rootProject 不存在 默认设置为false
        project.rootProject.setProperty(IS_RUN_ALONE, false)
        return false
    }

    //判断当前是否为壳APP
    private boolean isMainApp(Project project) {
        //1.仅以当前module下，并以之为准
        if (project.hasProperty(IS_MAIN_APP)) {
            return Boolean.parseBoolean((project.properties.get(IS_MAIN_APP)))
        }
        return false
    }

    /**
     * 自动添加依赖，只在运行assemble任务的才会添加依赖，因此在开发期间组件之间是完全感知不到的，这是做到完全隔离的关键
     */
    private void compileComponents(AssembleTask assembleTask, Project project) {
        //只有assemble才会添加依赖
        if (!assembleTask.isAssemble) return

        String components
        if (assembleTask.isDebug) {
            components = (String) project.properties.get("debugComponent")
        } else {
            components = (String) project.properties.get("releaseComponent")
        }

        if (components == null || components.trim().length() == 0) {
            log("there is no add dependencies ")
            return
        }

        String[] compileComponents = components.split(",")
        if (compileComponents == null || compileComponents.length == 0) {
            log("there is no add dependencies ")
            return
        }
        for (String str : compileComponents) {
            log("comp is " + str)
            str = str.trim()
            addComponent(str)
        }
    }

    //添加扩展方法
    private void addComponent(String dependenciesName) {
        if (assembleTask.isAssemble) {
            addDependencies(mProject, dependenciesName)
        }
    }

    //添加依赖
    private void addDependencies(Project project, String dependenciesName) {
        def dependencyMode = (project.gradle.gradleVersion as float) >= 4.1F ? 'api' : 'compile'
        dependenciesName = trimAll(dependenciesName)
        log("addDependencies: trimAll" + dependenciesName)
        if (dependenciesName.startsWith(':')) { //project
            log("addDependencies: project" + dependenciesName)
            //读取根目录下的
            project.dependencies.add(dependencyMode, project.project(dependenciesName))
        } else { //依赖
            log("addDependencies: dependencies" + dependenciesName)
            project.dependencies.add(dependencyMode, dependenciesName)
        }

    }

    //获得当前的module名字
    private String getModuleName(Project project) {
        return project.path.replace(":", "")
    }

    //实体类
    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false
    }

    /**
     * 日志打印
     * @param msg
     * @return
     */
    private log(String msg) {
        if (isDebug) System.out.println('thorAlone:' + msg)
    }

    //删除左右两边的不可见字符 增加配置容错性
    private String trimAll(String str) {
        char[] value = str.toCharArray()
        int len = value.length
        int st = 0
        char[] val = value

        while ((st < len) && (val[st] <= ' '
                ||  val[st] == ','
                ||  val[st] == '\\'
                ||  val[st] == '\r'
                ||  val[st] == '\n'
                ||  val[st] == '\''
                ||  val[st] == '\"')) {
            st++
        }
        while ((st < len) && (val[len - 1] <= ' '
                ||  val[len - 1] == ','
                ||  val[len - 1] == '\\'
                ||  val[len - 1] == '\r'
                ||  val[len - 1] == '\n'
                ||  val[len - 1] == '\''
                ||  val[len - 1] == '\"')) {
            len--
        }
        return ((st > 0) || (len < value.length)) ? str.substring(st, len) : str
    }
}