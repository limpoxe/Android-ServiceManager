package com.limpoxe.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.limpoxe.demo.api.API1;
import com.limpoxe.demo.api.API2;
import com.limpoxe.demo.api.API3;
import com.limpoxe.demo.service.Service1;
import com.limpoxe.demo.service.Service2;
import com.limpoxe.demo.service.Service3;
import com.limpoxe.support.servicemanager.ServiceManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TextView(this));

        //为了便于测试跨进程服务的效果
        //这里通过startService创建了3个进程，这几个服务本身无用。
        startService(new Intent(this, Service1.class));
        startService(new Intent(this, Service2.class));
        startService(new Intent(this, Service3.class));

    }


    @Override
    protected void onResume() {
        super.onResume();

        //测试api
        //创建进程需要时间，这里延长1.5秒
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取服务
                    API1 a1 = (API1) ServiceManager.getService("service1");
                    if (a1 != null) {
                        a1.login(111);
                    }
                    API2 a2 = (API2)ServiceManager.getService("service2");
                    if (a2 != null) {
                        a2.logout(222);
                    }
                    API3 a3 = (API3)ServiceManager.getService("service3");
                    if (a3 != null) {
                        a3.forget(333);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1500);

    }
}
