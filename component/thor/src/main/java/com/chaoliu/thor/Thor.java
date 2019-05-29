package com.chaoliu.thor;

import android.app.Application;

import com.chaoliu.thor.core.AppDelegateSingleton;
import com.chaoliu.thor.service.InitializeService;
import com.chaoliu.thor.util.Logger;

/**
 *
 * 初始化APP所有子组件的application
 *
 */
public class Thor {

    /**
     * 初始化
     * @param app
     */
    public static void initApp(Application app) {
        AppDelegateSingleton delegateSingleton = AppDelegateSingleton.getInstance();
        delegateSingleton.init( app );
        InitializeService.start( app );
    }

    public static void setLogEnable(boolean enable){
        Logger.setEnable( enable );
    }

}