package com.beyond.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public class SyncScheduleService  extends Service {

    private static final String action = "com.beyond.note7.intent.action.SYNC_SCHEDULE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerTime = System.currentTimeMillis() + 1000*10;
        Intent i = new Intent(this, SyncReceiver.class);
        i.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,  0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 1000*60*10, pi);
        return super.onStartCommand(intent, flags, startId);
    }
}
