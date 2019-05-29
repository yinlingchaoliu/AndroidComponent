package com.chaoliu.thor.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.chaoliu.thor.core.AppDelegateSingleton;

/**
 * 初始化
 */
public class InitializeService extends IntentService {

    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.chaoliu.thor.app.start";
    private static String TAG = InitializeService.class.getSimpleName();

    public InitializeService() {
        super( TAG );
    }

    public InitializeService(String name) {
        super( name );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals( action )) {
                performInit();
            }
        }
    }

    private void performInit() {
        AppDelegateSingleton.getInstance().onInitSlow();
    }

    public static void start(Context context) {
        Intent intent = new Intent( context, InitializeService.class );
        intent.setAction( ACTION_INIT_WHEN_APP_CREATE );
        context.startService( intent );
    }
}
