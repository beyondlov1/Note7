package com.beyond.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * @author: beyond
 * @date: 2021/9/28
 */
public class HttpServerService extends Service {

    private HttpServer httpServer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if ( httpServer == null){
                httpServer = new HttpServer(this);
            }
        } catch (IOException e) {
            Log.e("start http server", "onStartCommand: start http error", e );
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (httpServer!=null){
            httpServer.stop();
        }
        super.onDestroy();
    }
}
