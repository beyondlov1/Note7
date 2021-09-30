package com.beyond.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public class MyPermissionUtils {

    public  static boolean requestPermission(Context context, String... permissions) {
        return requestPermission(context, ()->{},permissions);
    }

    public  static boolean requestPermission(Context context, PermissionGotCallback runnable, String... permissions) {
        boolean got = checkPermission(context, permissions);
        if (got){
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        PermissionsUtil.requestPermission(context, new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permission) {
                requestPermission(context, runnable, permissions);
            }

            @Override
            public void permissionDenied(@NonNull String[] permission) {

            }
        },permissions);
        return false;
    }

    private static boolean checkPermission(Context context, @NonNull String[] requirePermissions) {
        boolean hasPermission = true;
        for (String requirePermission : requirePermissions) {
            hasPermission = hasPermission && ActivityCompat.checkSelfPermission(context, requirePermission) == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermission;
    }

    public interface PermissionGotCallback{
        void run() throws Exception;
    }
}
