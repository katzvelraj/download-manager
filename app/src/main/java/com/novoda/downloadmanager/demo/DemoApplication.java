package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;

import java.util.concurrent.TimeUnit;

public class DemoApplication extends Application {

    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        Handler handler = new Handler(Looper.getMainLooper());

        liteDownloadManagerCommands = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher_round)
                .withCallbackThrottleByTime(TimeUnit.MILLISECONDS, 1)
                .build();
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
