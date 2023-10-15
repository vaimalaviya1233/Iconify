package com.drdisagree.iconify;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.drdisagree.iconify.services.IRootServiceProvider;
import com.drdisagree.iconify.services.RootServiceProvider;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.ref.WeakReference;

public class Iconify extends Application {

    private static Iconify instance;
    private static IRootServiceProvider mRootServiceProvider;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        startRootService();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(Iconify.getInstance().getApplicationContext());
        }

        return contextReference.get();
    }

    private static Iconify getInstance() {
        return instance;
    }

    public static IRootServiceProvider getRootService() {
        if (mRootServiceProvider == null) {
            new Handler(Looper.getMainLooper()).post(Iconify::startRootService);
        }

        return mRootServiceProvider;
    }

    private static void startRootService() {
        Log.i("Iconify", "Starting RootService...");

        Intent intent = new Intent(getAppContext(), RootServiceProvider.class);
        ServiceConnection mCoreRootServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRootServiceProvider = IRootServiceProvider.Stub.asInterface(service);
                Log.i("Iconify", "RootService started.");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRootServiceProvider = null;
                Log.i("Iconify", "RootService stopped.");
            }
        };
        RootService.bind(intent, mCoreRootServiceConnection);
    }
}