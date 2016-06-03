package com.limpoxe.demo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.limpoxe.demo.api.API1Impl;
import com.limpoxe.demo.api.API2Impl;
import com.limpoxe.demo.api.API3Impl;
import com.limpoxe.support.servicemanager.ServiceManager;

/**
 * Created by cailiming on 16/6/4.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化manager
        ServiceManager.init(this);

        //为了测试，这里分别在3个进程注册1个服务
        String pName = getCurProcessName();
        if (pName.endsWith("p1")) {
            ServiceManager.publishService("service1", API1Impl.class.getName());
        } else if (pName.endsWith("p2")) {
            ServiceManager.publishService("service2", API2Impl.class.getName());
        } else if (pName.endsWith("p3")) {
            ServiceManager.publishService("service3", API3Impl.class.getName());
        }
    }

    String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }

}
