package com.limpoxe.support.servicemanager;

import android.app.Application;
import android.os.Bundle;
import android.os.Process;

import com.limpoxe.support.servicemanager.compat.BundleCompat;
import com.limpoxe.support.servicemanager.local.LocalServiceManager;

/**
 * Created by cailiming on 16/6/3.
 */
public class ServiceManager {

    public static Application sApplication;

    public static void init(Application application) {
        sApplication = application;

        Bundle argsBundle = new Bundle();
        int pid = Process.myPid();
        argsBundle.putInt("pid", pid);
        //为每个进程发布一个binder
        BundleCompat.putBinder(argsBundle, "binder", new ProcessBinder(ProcessBinder.class.getName() + "_" + pid));
        ServiceManager.sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                ServiceProvider.REPORT_BINDER, null, argsBundle);
    }

    public static Object getService(String name) {

        //首先在当前进程内查询
        Object service = LocalServiceManager.getService(name);

        if (service == null) {
            //向远端器查询
            Bundle bundle = sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                    ServiceProvider.QUERY_INTERFACE, name, null);

            if (bundle != null) {
                String interfaceClassName = bundle.getString(ServiceProvider.QUERY_INTERFACE_RESULT);

                if (interfaceClassName != null) {
                    service = RemoteProxy.getProxyService(name, interfaceClassName);
                    //缓存Proxy到本地
                    if (service != null) {
                        LocalServiceManager.registerInstance(name, service);
                    }
                }
            }
        }

        return service;
    }

    public static void publishService(String name, String serviceClass) {

        //先缓存到本地
        LocalServiceManager.registerClass(name, serviceClass);

        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt("pid", pid);
        try {
            String face = Class.forName(serviceClass).getInterfaces()[0].getName();
            argsBundle.putString("interface", face);
            //再发布到远端
            sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                    ServiceProvider.PUBLISH_SERVICE, name, argsBundle);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
