package com.beyond;

import android.app.Application;
import android.content.Intent;

import com.beyond.schedule.SyncScheduleService;
import com.beyond.server.HttpServerService;

import java.util.prefs.Preferences;

/**
 * @author: beyond
 * @date: 2021/9/29
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, SyncScheduleService.class);
        startService(intent);

        if (Preferences.userRoot().getBoolean("http_opened", false)){
            Intent intent2 = new Intent(this, HttpServerService.class);
            startService(intent2);
        }

    }
}
