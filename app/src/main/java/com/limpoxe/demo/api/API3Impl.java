package com.limpoxe.demo.api;

import android.util.Log;

/**
 * Created by cailiming on 16/6/4.
 */
public class API3Impl implements API3{

    public API3Impl() {
        Log.d("API3Impl", "API3Impl init()()");
    }

    public int forget(int a) {
        Log.d("API3Impl", "a=＝＝＝＝＝＝＝＝＝＝＝＝＝＝" + a);
        return a *2;
    }

}
