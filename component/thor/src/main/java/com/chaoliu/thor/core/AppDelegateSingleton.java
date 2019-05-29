package com.chaoliu.thor.core;

import android.app.Application;

import com.chaoliu.thor.util.ClassUtils;
import com.chaoliu.thor.util.Logger;

import java.util.List;

/**
 * @author chentong
 *
 * @date 2019-05-13
 * App初始化代理 前期为了快速搭建采用反射，
 * 后期为性能优化一律采用apt+asm
 */
public final class AppDelegateSingleton {

    //package root
    private static final String ROOT_PACKAGE = "com.chaoliu.thor.app";
    private List<IApplicationDelegate> mAppDelegateList;
    private Application delegateApp;
    private static final String TAG = "AppDelegateSingleton";

    private AppDelegateSingleton() {
    }

    private static class SingleHolder {
        private static final AppDelegateSingleton INSTANCE = new AppDelegateSingleton();
    }

    public static AppDelegateSingleton getInstance() {
        return SingleHolder.INSTANCE;
    }

    /**
     * @param app
     */
    public void init(Application app) {
        this.delegateApp = app;
        mAppDelegateList = ClassUtils.getObjectsWithInterface( app, IApplicationDelegate.class, ROOT_PACKAGE );
        Logger.d( TAG, "App  component created" );
        onInitSpeed( app );
    }

    /**
     * 快速初始化
     *
     * @param app
     */
    private void onInitSpeed(Application app) {
        for (IApplicationDelegate appDelegate : mAppDelegateList) {
            appDelegate.onInitSpeed( app );
        }
    }

    /**
     * 耗时初始化
     */
    public void onInitSlow() {
        Logger.d( TAG, "App  component  inited in the service" );
        for (IApplicationDelegate appDelegate : mAppDelegateList) {
            appDelegate.onInitSlow( delegateApp );
        }
    }

}