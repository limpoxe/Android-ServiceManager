package com.limpoxe.demo.api;

import android.util.Log;

/**
 * Created by cailiming on 16/6/4.
 */
public class API2Impl implements API2{

    public API2Impl() {
        Log.d("API2Impl", "API2Impl init()()");
    }

    public int logout(int a) {
        Log.d("API2Impl", "a=＝＝＝＝＝＝＝＝＝＝＝＝＝" + a);
        return a * 2;
    }

}
