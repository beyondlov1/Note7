package com.beyond;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import com.beyond.databinding.FragmentRemoteSettingBinding;
import com.beyond.jgit.GitLiteConfig;
import com.beyond.jgit.storage.SardineStorage;
import com.beyond.jgit.util.PathUtils;
import com.beyond.util.GitUtils;
import com.beyond.util.ToastUtil;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class RemoteSettingFragment extends Fragment {

    private FragmentRemoteSettingBinding binding;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        Handler handler = new Handler();

        binding = FragmentRemoteSettingBinding.inflate(inflater, container, false);

        Bundle arguments = getArguments();
        String repoRoot = arguments != null ? arguments.getString("root") : null;
        if (repoRoot == null){
            binding.login.setEnabled(false);
            return binding.getRoot();
        }

        try {
            GitLiteConfig config = GitUtils.getOrCreateRepo(repoRoot).getConfig();
            List<GitLiteConfig.RemoteConfig> configs = config.getRemoteConfigs();
            binding.configList.setAdapter(new CommonAdapter<GitLiteConfig.RemoteConfig>(getContext(),R.layout.list_item_config, configs) {
                @Override
                protected void convert(ViewHolder holder, GitLiteConfig.RemoteConfig remoteConfig, int position) {
                    holder.setText(R.id.item_remote_name, remoteConfig.getRemoteName());
                    holder.setText(R.id.item_remote_path, remoteConfig.getRemoteUrl());
                    holder.setText(R.id.item_username, remoteConfig.getRemoteUserName());
                    holder.setOnClickListener(R.id.item_data_container, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            binding.remoteName.setText(remoteConfig.getRemoteName());
                            binding.remotePath.setText(remoteConfig.getRemoteUrl());
                            binding.username.setText(remoteConfig.getRemoteUserName());
                            binding.password.setText(remoteConfig.getRemotePassword());
                        }
                    });
                    holder.setOnClickListener(R.id.item_delete, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            config.getRemoteConfigs().removeIf(x -> {
                                String remoteNameInConfig = x.getRemoteName();
                                return StringUtils.equals(remoteNameInConfig, remoteConfig.getRemoteName());
                            });
                            try {
                                config.save();
                                notifyItemRemoved(position);
                            } catch (IOException e) {
                                Log.i("RemoteSettingFragment", "save config error");
                            }
                        }
                    });
                }
            });
            binding.configList.setLayoutManager(new LinearLayoutManager(getContext()));



            binding.login.setEnabled(true);
            binding.login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String remoteName = binding.remoteName.getText().toString();
                    String remotePath = binding.remotePath.getText().toString();
                    String username = binding.username.getText().toString();
                    String password = binding.password.getText().toString();
                    binding.login.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (remotePath.startsWith("https")|| remotePath.startsWith("http")){
                                SardineStorage sardineStorage = new SardineStorage(remotePath, username, password);
                                try {
                                    sardineStorage.mkdir(".auth");
                                    sardineStorage.delete(".auth");
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            GitLiteConfig.RemoteConfig remoteConfig = new GitLiteConfig.RemoteConfig(remoteName, remotePath, username, password);
                                            remoteConfig.setRemoteTmpDir(PathUtils.concat(config.getGitDir(),"tmp"));
                                            config.getRemoteConfigs().removeIf(x -> StringUtils.equals(x.getRemoteName(), remoteConfig.getRemoteName()));
                                            config.getRemoteConfigs().add(remoteConfig);
                                            try {
                                                config.save();
                                            } catch (IOException e) {
                                                ToastUtil.toast(getContext(), "save config fail");
                                                config.getRemoteConfigs().remove(remoteConfig);
                                            }
                                            binding.configList.getAdapter().notifyDataSetChanged();
                                            binding.login.setEnabled(true);
                                            binding.remoteName.setText(null);
                                            binding.remotePath.setText(null);
                                            binding.username.setText(null);
                                            binding.password.setText(null);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.login.setEnabled(true);
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.remotePath.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    showListPopupWindow();
                }
            }
        });
        return binding.getRoot();
    }

    private void showListPopupWindow() {
        final String[] showArray = {"坚果云", "TeraCloud"};//要填充的数据
        final String[] dataArray = {"https://dav.jianguoyun.com/dav/", "https://yura.teracloud.jp/dav/"};//要填充的数据
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(getContext());
        listPopupWindow.setAdapter(new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_list_item_1,
                showArray) {
        });//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(binding.remotePath);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(false);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                binding.remotePath.setText(dataArray[i]);//把选择的选项内容展示在EditText上
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}