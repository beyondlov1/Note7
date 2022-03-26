package com.beyond;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.beyond.databinding.FragmentEditBinding;
import com.beyond.jgit.util.PathUtils;
import com.beyond.util.CalendarUtils;
import com.beyond.util.InputMethodUtil;
import com.beyond.util.MyPermissionUtils;
import com.beyond.util.TodoUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class EditFragment extends Fragment {

    private FragmentEditBinding binding;

    private AlertDialog alertDialog;

    private String initContent;

    private String repoRoot;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEditBinding.inflate(inflater, container, false);

        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }
        String name = arguments.getString("name");
        binding.nameEditText.setText(name);

        String path = arguments.getString("path");
        String content = null;
        if (StringUtils.isNotBlank(path)) {
            if (new File(path).exists()) {
                try {
                    content = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        repoRoot = arguments.getString("root");

        binding.contentEditText.setText(content);
        initContent = content;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.nameEditText.getText().toString().endsWith(".todo")) {
                    MyPermissionUtils.requestPermission(getContext(),
                            () -> {
                                save();
                                back();
                            },
                            Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
                }else{
                    save();
                    back();
                }
            }
        });

        binding.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.contentEditText.setText(null);
            }
        });
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("是否保存?")
                .setCancelable(true)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (binding.nameEditText.getText().toString().endsWith(".todo")) {
                            MyPermissionUtils.requestPermission(getContext(),
                                    () -> {
                                        save();
                                        back();
                                    },
                                    Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
                        }else{
                            save();
                            back();
                        }
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        back();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        back();
                    }
                }).create();

    }

    private void save() {
        String name = binding.nameEditText.getText().toString();
        if (StringUtils.isBlank(name) || StringUtils.isBlank(repoRoot)) {
            return;
        }
        File file = new File(PathUtils.concat(repoRoot, name));
        try {
            String content = binding.contentEditText.getText().toString();
            if (StringUtils.isNotBlank(content)) {
                List<Todo> parsed = TodoUtils.parse(content);
                boolean createdNewReminder = false;
                for (Todo todo : parsed) {
                    createdNewReminder = createdNewReminder || this.setupReminder(todo);
                }
                String newContent = TodoUtils.format(parsed);
                if (!StringUtils.equals(newContent, initContent)) {
                    if (!name.endsWith(".todo") && createdNewReminder) {
                        File todoFile = new File(PathUtils.concat(repoRoot, name + ".todo"));
                        FileUtils.writeStringToFile(todoFile, newContent, StandardCharsets.UTF_8);
                        FileUtils.deleteQuietly(file);
                    }else{
                        FileUtils.writeStringToFile(file, newContent, StandardCharsets.UTF_8);
                    }
                }
            } else {
                FileUtils.deleteQuietly(file);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean setupReminder(Todo todo) {
        if (todo.getRemindTime() != null && todo.getSource() != Todo.Source.READ) {
            CalendarUtils.insertEventAndReminder(getContext(), todo.getOriginText(), null, todo.getRemindTime());
            return true;
        }
        return false;
    }

    private void back() {
        NavHostFragment.findNavController(EditFragment.this)
                .navigateUp();

        View view = binding.saveButton;
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public boolean onBackPressed() {
        if (initContent == null) {
            back();
            return true;
        }
        if (!StringUtils.equals(initContent, binding.contentEditText.getText().toString())) {
            alertDialog.show();
        } else {
            back();
        }
        return true;
    }
}