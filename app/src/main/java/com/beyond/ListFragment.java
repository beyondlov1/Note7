package com.beyond;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.databinding.FragmentListBinding;
import com.beyond.entity.ListItem;
import com.beyond.jgit.util.FileUtil;
import com.beyond.jgit.util.PathUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;

    private int currPosition;

    String repoRoot;

    private CommonAdapter<ListItem> adapter;

    List<ListItem> data = new ArrayList<>();

    Handler handler = new Handler();

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListBinding.inflate(inflater, container, false);

        Bundle arguments = getArguments();
        if (arguments == null){
            return binding.getRoot();
        }
        repoRoot = arguments.getString("root");
        if (repoRoot == null){
            return binding.getRoot();
        }

        RecyclerView listView = binding.list;
        adapter = new CommonAdapter<ListItem>(getContext(), R.layout.list_item, data) {
            @Override
            protected void convert(ViewHolder holder, ListItem item, int position) {
                holder.setText(R.id.list_item_name, item.getName());
                holder.setText(R.id.list_item_content, item.getContent());

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(13);
                gradientDrawable.setStroke(1, ContextCompat.getColor(mContext, R.color.dark_gray));
                View card = holder.getView(R.id.list_item_card);
                card.setBackground(gradientDrawable);

                holder.setOnClickListener(R.id.list_item, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", item.getName());
                        bundle.putString("path", item.getAbsPath());
                        bundle.putString("root", repoRoot);
                        NavHostFragment.findNavController(ListFragment.this)
                                .navigate(R.id.action_ListFragment_to_EditFragment, bundle);
                    }
                });
            }
        };
        listView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(linearLayoutManager);


        binding.addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                String name = DateFormatUtils.format(new Date(), "yyyyMMdd_HHmm");
                bundle.putString("name", name);

                String path = PathUtils.concat(repoRoot, name);
                bundle.putString("path", path);

                bundle.putString("root", repoRoot);

                NavHostFragment.findNavController(ListFragment.this)
                        .navigate(R.id.action_ListFragment_to_EditFragment, bundle);
            }
        });

        initData();

        ContextHolder.setCurrentListFragment(this);
        return binding.getRoot();

    }

    private void initData() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<ListItem> tmpData = new ArrayList<>();
                List<File> files = FileUtil.listChildFilesAndDirs(repoRoot, x -> !x.getName().startsWith(".")).stream().filter(File::isFile).sorted(Comparator.comparing(x -> {
                    try {
                        return Files.getLastModifiedTime(x.toPath()).toMillis();
                    } catch (IOException e) {
                        return 0L;
                    }
                },Comparator.reverseOrder())).collect(Collectors.toList());
                for (File file : files) {
                    try {
                        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                        tmpData.add(new ListItem(file.getName(), content, file.getAbsolutePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        data.clear();
                        data.addAll(tmpData);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList() {
        initData();
    }
}