package com.beyond;

import android.app.Application;
import android.content.Intent;

import com.beyond.schedule.SyncScheduleService;

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

    }
}
