package com.beyond;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.databinding.FragmentListBinding;
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

    private CommonAdapter<String> adapter;

    List<String> data = new ArrayList<>();

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

        initData();

        RecyclerView listView = binding.list;
        adapter = new CommonAdapter<String>(getContext(), R.layout.list_item, data) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.list_item_name, s);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(13);
                gradientDrawable.setStroke(1, ContextCompat.getColor(mContext, R.color.dark_gray));
                View card = holder.getView(R.id.list_item_card);
                card.setBackground(gradientDrawable);

                final String name = data.get(position);
                holder.setText(R.id.list_item_name, name);

                File file = new File(PathUtils.concat(repoRoot, name));
                String content = "";
                if (!file.exists()) {
                    return;
                }
                try {
                    content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.setText(R.id.list_item_content, content);


                holder.setOnClickListener(R.id.list_item, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", name);
                        bundle.putString("path", file.getAbsolutePath());
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

        ContextHolder.setCurrentListFragment(this);
        return binding.getRoot();

    }

    private void initData() {
        data.clear();
        List<File> files = FileUtil.listChildFilesAndDirs(repoRoot, x -> !x.getName().startsWith(".")).stream().filter(File::isFile).sorted(Comparator.comparing(x -> {
            try {
                return Files.getLastModifiedTime(x.toPath()).toMillis();
            } catch (IOException e) {
                return 0L;
            }
        },Comparator.reverseOrder())).collect(Collectors.toList());
        for (File file : files) {
            data.add(file.getName());
        }
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
        adapter.notifyDataSetChanged();
    }
}