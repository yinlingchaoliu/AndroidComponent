package com.chaoliu.thor.api

import org.gradle.api.Project;

public class Utils {

    //删除左右两边的不可见字符 增加配置容错性
    public static String trimAll(String str) {
        char[] value = str.toCharArray()
        int len = value.length
        int st = 0
        char[] val = value

        while ((st < len) && (val[st] <= ' '
                || val[st] == ','
                || val[st] == '\\'
                || val[st] == '\r'
                || val[st] == '\n'
                || val[st] == '\''
                || val[st] == '\"')) {
            st++
        }
        while ((st < len) && (val[len - 1] <= ' '
                || val[len - 1] == ','
                || val[len - 1] == '\\'
                || val[len - 1] == '\r'
                || val[len - 1] == '\n'
                || val[len - 1] == '\''
                || val[len - 1] == '\"')) {
            len--
        }
        return ((st > 0) || (len < value.length)) ? str.substring(st, len) : str
    }

    //获得指定项目java目录
    public static String getModuleJavaPath(Project project, String moudleName) {
        return project.rootProject.project(moudleName).projectDir.path + "/src/main/java"
    }

    //获得当前项目java目录
    public static String getProjectJavaPath(Project project) {
        return project.projectDir.path + "/src/main/java"
    }

    /**
     * 清除空目录
     * @param dir
     */
    public static void clearEmptyDir(File dir) {
        File[] dirs = dir.listFiles()
        for (File file : dirs) {
            if (file.isDirectory()) {
                clearEmptyDir(file)
            }
        }

        if (dir.isDirectory() && dir.delete())
            println('weixinApi:文件夹' + dir.path + "清理成功")
    }

}