package com.limpoxe.support.servicemanager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;

import com.limpoxe.support.servicemanager.compat.BundleCompat;
import com.limpoxe.support.servicemanager.local.LocalServiceManager;

/**
 * Created by cailiming on 16/6/3.
 */
public class ServiceManager {

    public static final String ACTION_SERVICE_DIE_OR_CLEAR = "com.limpoxe.support.action.SERVICE_DIE_OR_CLEAR";

    public static Application sApplication;

    public static void init(Application application) {
        sApplication = application;

        Bundle argsBundle = new Bundle();
        int pid = Process.myPid();
        argsBundle.putInt(ServiceProvider.PID, pid);
        //为每个进程发布一个binder
        BundleCompat.putBinder(argsBundle, ServiceProvider.BINDER, new ProcessBinder(ProcessBinder.class.getName() + "_" + pid));
        ServiceManager.sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                ServiceProvider.REPORT_BINDER, null, argsBundle);

        ServiceManager.sApplication.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //服务进程挂掉以后 或者服务进程主动通知清理时,移除客户端的代理缓存
                LocalServiceManager.unRegister(intent.getStringExtra(ServiceProvider.NAME));
            }
        }, new IntentFilter(ACTION_SERVICE_DIE_OR_CLEAR));
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

    //给当前进程发布一个服务, 发布后其他进程可使用此服务
    public static void publishService(String name, String serviceClass) {

        //先缓存到本地
        LocalServiceManager.registerClass(name, serviceClass);

        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt(ServiceProvider.PID, pid);
        try {
            String face = Class.forName(serviceClass).getInterfaces()[0].getName();
            argsBundle.putString(ServiceProvider.INTERFACE, face);
            //再发布到远端
            sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                    ServiceProvider.PUBLISH_SERVICE, name, argsBundle);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //清理当前进程发布的所有服务
    public static void unPublishAllService() {
        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt(ServiceProvider.PID, pid);
        sApplication.getContentResolver().call(ServiceProvider.buildUri(),
                ServiceProvider.UNPUBLISH_SERVICE, null, argsBundle);
    }

}
