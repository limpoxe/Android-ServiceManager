package com.limpoxe.support.servicemanager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;

import com.limpoxe.support.servicemanager.compat.BundleCompat;
import com.limpoxe.support.servicemanager.compat.ContentProviderCompat;
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
        ContentProviderCompat.call(ServiceProvider.buildUri(),
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
        return getService(name, ServiceManager.class.getClassLoader());
    }

    /**
     *
      * @param name
     * @param interfaceClassloader
     * @return
     */
    public static Object getService(String name, ClassLoader interfaceClassloader) {

        //首先在当前进程内查询
        Object service = LocalServiceManager.getService(name);

        if (service == null) {
            //向远端器查询
            Bundle bundle = ContentProviderCompat.call(ServiceProvider.buildUri(),
                    ServiceProvider.QUERY_INTERFACE, name, null);

            if (bundle != null) {
                String interfaceClassName = bundle.getString(ServiceProvider.QUERY_INTERFACE_RESULT);

                if (interfaceClassName != null) {
                    service = RemoteProxy.getProxyService(name, interfaceClassName, interfaceClassloader);
                    //缓存Proxy到本地
                    if (service != null) {
                        LocalServiceManager.registerInstance(name, service);
                    }
                }
            }
        }

        return service;
    }

    /**
     * 给当前进程发布一个服务, 发布后其他进程可使用此服务
     */
    public static void publishService(String name, String className) {
        publishService(name, className, ServiceManager.class.getClassLoader());
    }

    /**
     * 给当前进程发布一个服务, 发布后其他进程可使用此服务
     */
    public static void publishService(String name, final String className, final ClassLoader classloader) {
        publishService(name, new LocalServiceManager.ClassProvider() {
            @Override
            public Class getServiceClass() {
                try {
                    return classloader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public String getInterfaceName() {
                return getServiceClass().getInterfaces()[0].getName();
            }
        });
    }

    /**
     * 给当前进程发布一个服务, 发布后其他进程可使用此服务
     */
    public static void publishService(String name, final LocalServiceManager.ClassProvider provider) {

        //先缓存到本地
        LocalServiceManager.registerClass(name, provider);

        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt(ServiceProvider.PID, pid);

        //classLoader
        String serviceInterfaceClassName = provider.getInterfaceName();
        argsBundle.putString(ServiceProvider.INTERFACE, serviceInterfaceClassName);
        //再发布到远端
        ContentProviderCompat.call(ServiceProvider.buildUri(),
                ServiceProvider.PUBLISH_SERVICE, name, argsBundle);

    }

    /**
     * 清理当前进程发布的所有服务
     */
    public static void unPublishAllService() {
        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt(ServiceProvider.PID, pid);
        ContentProviderCompat.call(ServiceProvider.buildUri(),
                ServiceProvider.UNPUBLISH_SERVICE, null, argsBundle);
    }

    public static void unPublishService(String name) {
        int pid = Process.myPid();
        Bundle argsBundle = new Bundle();
        argsBundle.putInt(ServiceProvider.PID, pid);
        argsBundle.putString(ServiceProvider.NAME, name);
        ContentProviderCompat.call(ServiceProvider.buildUri(),
                ServiceProvider.UNPUBLISH_SERVICE, null, argsBundle);
    }

}
