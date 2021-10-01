package com.beyond;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.beyond.databinding.ActivityMainBinding;
import com.beyond.jgit.util.PathUtils;
import com.beyond.util.GitUtils;
import com.beyond.util.ToastUtil;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private String repoRoot;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        repoRoot = PathUtils.concat(this.getFilesDir().getAbsolutePath(), "test-repo");
        new File(repoRoot).mkdirs();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        Bundle bundle = new Bundle();
        bundle.putString("root",repoRoot);
        navController.setGraph(R.navigation.nav_graph,bundle);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


//        binding.fab.setOnClickListener(new android.view.View.OnClickListener() {
//
//            int count = 0;
//
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//                File filesDir = getFilesDir();
//                File file = new File(filesDir, "hello.txt");
//                try {
//                    FileUtils.writeStringToFile(file,"i am write by beyond"+count, StandardCharsets.UTF_8);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                count++;
//
//
//            }
//        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (Preferences.userRoot().getBoolean("last_sync_success", true)){
            MenuItem menuItem = menu.findItem(R.id.action_sync_now);
            menuItem.setIcon(R.drawable.baseline_sync_white_24dp);
        }else{
            MenuItem menuItem =  menu.findItem(R.id.action_sync_now);
            menuItem.setIcon(R.drawable.baseline_sync_problem_yellow_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync_now) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.toast(MainActivity.this, "sync started", Toast.LENGTH_LONG);;
                            }
                        });
                        GitUtils.sync(repoRoot);
                        Preferences.userRoot().putBoolean("last_sync_success", true);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ListFragment currentListFragment = ContextHolder.getCurrentListFragment();
                                if (currentListFragment != null){
                                    currentListFragment.refreshList();
                                }
                                ToastUtil.toast(MainActivity.this, "sync success", Toast.LENGTH_LONG);
                            }
                        });
                    } catch (Exception e) {
                        Preferences.userRoot().putBoolean("last_sync_success", false);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.toast(MainActivity.this, "sync error", Toast.LENGTH_LONG);
                            }
                        });
                        Log.e("MainActivity", "sync error");
                    }
                }
            }).start();
            return true;
        }

        if (id == R.id.action_remote_repo_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("root", repoRoot);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        Bundle bundle = new Bundle();
        bundle.putString("root",repoRoot);
        navController.setGraph(R.navigation.nav_graph,bundle);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Fragment navHostFragment = FragmentManager.findFragment(findViewById(R.id.nav_host_fragment_content_main));
        List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            /*如果是自己封装的Fragment的子类  判断是否需要处理返回事件*/
            if (fragment instanceof EditFragment) {

                if (((EditFragment) fragment).onBackPressed()) {
                    /*在Fragment中处理返回事件*/
                    return;
                }
            }
        }
        super.onBackPressed();
    }


}