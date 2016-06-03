package com.limpoxe.demo.api;

import android.util.Log;

/**
 * Created by cailiming on 16/6/4.
 */
public class API1Impl implements API1 {

    public API1Impl() {
        Log.d("API1Impl", "API1Impl init()()");
    }

    public int login(int a) {
        Log.d("API1Impl", "a=＝＝＝＝＝＝＝＝＝＝＝" + a);
        return  a *2;
    }

}
