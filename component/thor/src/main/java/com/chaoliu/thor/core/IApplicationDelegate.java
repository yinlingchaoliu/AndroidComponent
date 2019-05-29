package com.chaoliu.thor.core;

import android.app.Application;

/**
 * @author chentong
 * @date 2019-5-13
 * Application初始化代理类
 */
public interface IApplicationDelegate {
    /**
     * 快速初始化
     *
     * @param app 放在application onCreate里面
     */
    void onInitSpeed(Application app);


    /**
     * 耗时初始化
     *
     * @param app 放在intentService里面
     */
    void onInitSlow(Application app);
}