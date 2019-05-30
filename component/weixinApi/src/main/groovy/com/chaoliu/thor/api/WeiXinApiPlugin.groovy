package com.chaoliu.thor.api

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * 根据微信去api化方案获得启发
 * 采用声明式编程
 *
 * 步骤如下:
 * 1、将所有".api"结尾的文件，拷贝到指定module
 * 2、引用moulde ,compile module
 * 3、编译时移除所有".api"结尾文件
 *
 * @author:chentong
 * @date: 2019-5-16
 *
 */
public class WeiXinApiPlugin implements Plugin<Project> {

    private static final String API_MODULE = "ApiModule"
    private static final String GROUP_ID = "weixinapi"
    private static final String IS_RUN_ALWAYS="isRunAlways"

    private String moduleName
    private AssembleTask assembleTask
    private Project mProject
    private boolean isDebug = true
    private boolean isRunAlways = true

    void apply(Project project) {
        this.mProject = project

        //检查当前环境变量
        checkEnv(project)

        //获得配置中moduleName
        moduleName = getModuleName(project)

        //判断当前任务 只有assemble任务才添加依赖
        assembleTask = getTaskInfo(project)

        //添加扩展方法 (选项) 参数 只在运行assemble任务的才会添加依赖
        project.ext.addComponent = { dependencyName -> addComponent(dependencyName) }

        //移除api文件
        excludeAllApiFiles()

        //增加三个任务
        addApiModuleTask(project)

        //在项目配置好，初始化之前执行
        project.afterEvaluate {
            //如果项目是状态是isRunAlways=true 且 当前task不是ApiModule执行，避免重复执行
            if (getIsRunAlway(project) && isNotEqualTask(project, API_MODULE)){
                //清空所有文件
                deleteApiModule()
                //拷贝api文件
                copyApiModule()
                //删除空文件夹
                clearApiModuleEmptyDir()
            }
        }

    }

    //移除所有api文件
    private void excludeAllApiFiles() {
        for(Project project : mProject.getAllprojects()){
            excludeApiFiles(project)
        }
    }

    private void excludeApiFiles(Project project) {

        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)

        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else if(hasLib){
            variants = project.android.libraryVariants
        }else {
            return
        }

        variants.sourceSets {
            main {
                java {
                    exclude('**/**.api')
                }
            }
        }
    }

    /**
     * 初始化必要状态
     * @param project
     * @param configName
     */
    private void checkEnv(Project project) {
        //1.检查env
        if (!project.rootProject.hasProperty(API_MODULE)) {
            //若依然不存在 则设置isRunAlone 为false
            throw RuntimeException("please set " + API_MODULE + " in the root properties")
        }
    }

    private String getModuleName(Project project) {
        String moduleName = project.rootProject.properties.get(API_MODULE)
        return Utils.trimAll(moduleName)
    }

    private Boolean getIsRunAlway(Project project) {
        //1.没有IS_RUN_ALWAYS 默认为true
        if (!project.rootProject.hasProperty(IS_RUN_ALWAYS)) {
            log("if not set ${IS_RUN_ALWAYS} in the root properties, default value is true!!!")
            return true
        }
        return Boolean.parseBoolean(project.rootProject.properties.get(IS_RUN_ALWAYS))
    }

    //实体类
    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false
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

    //当前运行任务是不是指定task
    private boolean isEqualTask(Project project, String taskName) {
        List<String> taskNames = project.gradle.startParameter.taskNames
        String TASK_NAME = taskName.toUpperCase()
        for (String task : taskNames) {
            if (task.toUpperCase().contains(TASK_NAME)) {
                return true
            }
        }
        return false
    }

    private boolean isNotEqualTask(Project project, String taskName){
        return !isEqualTask(project,taskName)
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
        dependenciesName = Utils.trimAll(dependenciesName)
        if (dependenciesName.startsWith(':')) { //project
            log("addDependencies: project" + dependenciesName)
            //读取根目录下的
            project.dependencies.add(dependencyMode, project.project(dependenciesName))
        } else { //依赖
            log("addDependencies: dependencies" + dependenciesName)
            project.dependencies.add(dependencyMode, dependenciesName)
        }

    }

    private void addApiModuleTask(Project project) {

        project.tasks.create(name:"weiXin${API_MODULE}") {
            setGroup(GROUP_ID)
            setDescription("weixinApi一键执行")
        }.doFirst {
            deleteApiModule()
            copyApiModule()
            clearApiModuleEmptyDir()
        }

//        //跳过
//        project.rootProject.gradle.taskGraph.whenReady { taskGraph ->
//            taskGraph.allTasks.each { task ->
//                if (task.name.contains(API_MODULE)) {
//                    task.enabled = false
//                }
//            }
//        }
    }

    //删除ApiModule文件
    private void deleteApiModule() {
        String filePath = Utils.getModuleJavaPath(mProject, moduleName)
        mProject.delete filePath
        log("delete ${API_MODULE}")
    }

    //拷贝至ApiModule
    private void copyApiModule() {
        log("copyto ${API_MODULE}")

        mProject.copy {

            //遍历所有项目 将api文件
            for (Project project : mProject.rootProject.getAllprojects()) {
                String currentProjectPath = Utils.getProjectJavaPath(project)
                from(currentProjectPath) {
                    include '**/**.api'
                }
            }

            String apiModulePath = Utils.getModuleJavaPath(mProject, moduleName)

            into(apiModulePath)

            //将".api"改名为".java"
            rename { String filename ->
                int index = filename.indexOf(".api")
                String name = filename[0..index] + "java"
                return name
            }

        }

    }

    //清理ApiModule空文件夹
    private void clearApiModuleEmptyDir() {
        String filePath = Utils.getModuleJavaPath(mProject, moduleName)
        Utils.clearEmptyDir(new File(filePath))
        log("clear  ${API_MODULE}")
    }

    /**
     * 日志打印
     * @param msg
     * @return
     */
    private log(String msg) {
        if (isDebug) System.out.println('weixinApi:' + msg)
    }

}