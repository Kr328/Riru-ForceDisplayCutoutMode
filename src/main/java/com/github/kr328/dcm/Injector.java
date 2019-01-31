package com.github.kr328.dcm;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.IServiceManager;
import android.os.ServiceManager;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Injector {
    public static void inject() {
        Log.i(Global.TAG ,"Enter system_server");

        try {
            Method getIServiceManager = ServiceManager.class.getDeclaredMethod("getIServiceManager");
            Field  serviceManager = ServiceManager.class.getDeclaredField("sServiceManager");

            getIServiceManager.setAccessible(true);
            serviceManager.setAccessible(true);

            serviceManager.set(null , InterfaceProxy.createInterfaceProxy((IServiceManager)getIServiceManager.invoke(null) , new Class[]{IServiceManager.class} ,Injector::onIServiceManagerCalled));
        } catch (Exception e) {
            Log.e(Global.TAG ,"Proxy ServiceManager failure" ,e);
        }
    }

    private static Object onIServiceManagerCalled(IServiceManager original ,IServiceManager replaced ,Method method ,Object[] args) throws Throwable {
        switch ( method.getName() ) {
            case "addService" :
                onAddService((String)args[0] ,(IBinder) args[1]);
        }
        return method.invoke(original ,args);
    }

    private static void onAddService(String name , IBinder service) {
        if ( !"window".equals(name) || !"com.android.server.wm.WindowManagerService".equals(service.getClass().getName()) ) {
            Log.i(Global.TAG ,"Service " + name + "#" + service.getClass().getName() + " not match");
            return;
        }
        Log.i(Global.TAG ,"Window service found.");

        try {
            initWindowServiceReplace(service);
        } catch (Exception e) {
            Log.e(Global.TAG ,"Replace windows manager failure." ,e);
        }
    }

    private static void initWindowServiceReplace(IBinder service) throws Exception {
        Class<?> windowServiceClass = service.getClass();
        Field policyField = windowServiceClass.getDeclaredField("mPolicy");

        policyField.setAccessible(true);

        Object policy = policyField.get(service);

        Class<?> windowsManagerPolicyInterface = policy.getClass().getClassLoader().loadClass("com.android.server.policy.WindowManagerPolicy");

        policyField.set(service ,InterfaceProxy.createInterfaceProxy(policy ,new Class<?>[]{windowsManagerPolicyInterface} , Injector::onWindowPolicyCalled));
    }

    private static Object onWindowPolicyCalled(Object original ,Object replaced ,Method method ,Object[] args) throws Throwable {
        switch (method.getName()) {
            case "adjustWindowParamsLw" :
                onAdjustWindowParamsLwCalled(args[0] , (WindowManager.LayoutParams) args[1]);
        }

        return method.invoke(original ,args);
    }

    private static void onAdjustWindowParamsLwCalled(Object windowState , WindowManager.LayoutParams layoutParams) throws Throwable {
        synchronized (Injector.class) {
            if ( windowStateGetPackageName == null ) {
                windowStateGetPackageName = windowState.getClass().getDeclaredMethod("getOwningPackage");
                windowStateGetPackageName.setAccessible(true);
            }

            if ( cutoutModeManager == null ) {
                cutoutModeManager = new CutoutModeManager();
            }
        }

        String packageName = windowStateGetPackageName.invoke(windowState).toString();

        layoutParams.layoutInDisplayCutoutMode = cutoutModeManager.notePackageCutout(packageName
                ,layoutParams.layoutInDisplayCutoutMode);
    }

    private static Method windowStateGetPackageName;
    private static CutoutModeManager cutoutModeManager;
}
