package com.example.kristavanderhorst.translite;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Krista Vanderhorst on 3/28/2018.
 */

public class SingletonRequestQueue {
    private static SingletonRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private SingletonRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized SingletonRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SingletonRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
