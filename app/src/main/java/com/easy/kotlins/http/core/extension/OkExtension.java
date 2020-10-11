package com.easy.kotlins.http.core.extension;


import com.easy.kotlins.http.core.OkFaker;

/**
 * Create by LiZhanPing on 2020/8/26
 */
public abstract class OkExtension {

    private OkFaker mOkFaker;


    public OkFaker getOkFaker() {
        if(mOkFaker == null){
            throw new NullPointerException("you must install extension to OkFaker");
        }
        return mOkFaker;
    }


    public final void install(OkFaker faker) {
        mOkFaker = faker;
    }

}
