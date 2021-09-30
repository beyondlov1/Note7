package com.beyond.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beyond.ContextHolder;
import com.beyond.ListFragment;
import com.beyond.util.GitUtils;
import com.beyond.util.ToastUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public class SyncReceiver extends BroadcastReceiver {


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final AtomicLong lastStart = new AtomicLong(0);
    private static final AtomicLong lastEnd = new AtomicLong(0);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (lastStart.get() > lastEnd.get() ){
            Log.i("SyncReceiver", "last is running, ignore");
            return;
        }
        if (System.currentTimeMillis() - lastEnd.get() < 1000*60){
            Log.i("SyncReceiver", "ignore");
            return;
        }
        Log.i("SyncReceiver", "sync schedule triggered");
        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                lastStart.set(System.currentTimeMillis());
                File root = context.getFilesDir();
                try {
                    List<File> repoDirs = Files.list(root.toPath())
                            .map(Path::toFile)
                            .filter(File::isDirectory)
                            .filter(x -> {
                                String[] list = x.list();
                                if (list != null && ArrayUtils.isNotEmpty(list)) {
                                    return Arrays.asList(list).contains(".git");
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                    for (File repoDir : repoDirs) {
                        GitUtils.sync(repoDir.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lastEnd.set(System.currentTimeMillis());
            }
        });

        try {
            ToastUtil.toast(context, "auto sync start");
            future.get();
            ListFragment currentListFragment = ContextHolder.getCurrentListFragment();
            if (currentListFragment != null){
                currentListFragment.refreshList();
            }
            ToastUtil.toast(context, "auto sync complete");
        } catch (ExecutionException | InterruptedException e) {
            Log.e("SyncReceiver", "auto sync error", e);
            ToastUtil.toast(context, "auto sync error");
        }
    }
}
