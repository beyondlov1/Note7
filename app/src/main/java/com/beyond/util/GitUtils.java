package com.beyond.util;

import android.util.Log;

import com.beyond.jgit.GitLite;
import com.beyond.jgit.GitLiteConfig;
import com.beyond.jgit.util.PathUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public abstract class GitUtils {

    public static GitLite getOrCreateRepo(String repoAbsPath) throws IOException {
        return getOrCreateRepo(repoAbsPath, "beyond", "beyond@note.com");
    }

    public static GitLite getOrCreateRepo(String repoAbsPath, String committerName, String committerEmail) throws IOException {
        if (StringUtils.isBlank(repoAbsPath)) {
            throw new RuntimeException("repoAbsPath is null");
        }
        GitLite git;
        if (new File(PathUtils.concat(repoAbsPath, ".git", "config.json")).exists()) {
            GitLiteConfig config = GitLiteConfig.readFrom(repoAbsPath);
            if (StringUtils.isNotBlank(committerName) && StringUtils.isNotBlank(committerEmail)) {
                config.setCommitterName(committerName);
                config.setCommitterEmail(committerEmail);
                config.save();
            }
            git = config.build();
        } else {
            git = GitLiteConfig.simpleConfig(repoAbsPath, committerName, committerEmail)
                    .save()
                    .build();
            git.init();
        }
        return git;
    }

    public static void sync(String repoRoot) throws IOException {
        Log.i("sync", "sync start .. ");
        GitLite git = getOrCreateRepo(repoRoot);
        GitLiteConfig config = git.getConfig();
        List<GitLiteConfig.RemoteConfig> remoteConfigs = config.getRemoteConfigs();
        if (CollectionUtils.isNotEmpty(remoteConfigs)) {
            for (GitLiteConfig.RemoteConfig remoteConfig : remoteConfigs) {
                syncOneRepoWith(git, remoteConfig);
            }
        }
        Log.i("sync", "sync end .. ");
    }

    private static void syncOneRepoWith(GitLite git, GitLiteConfig.RemoteConfig remoteConfig) throws IOException {
        Log.i("syncOneRemote", "sync start .. "+remoteConfig.getRemoteUrl());
        String remoteName = remoteConfig.getRemoteName();
        git.init();
        git.add();
        git.commit("auto commit");
        git.fetch(remoteName);
        git.merge(remoteName);
        git.checkout();
        git.packAndPush(remoteName);
        Log.i("syncOneRemote", "sync end .. "+remoteConfig.getRemoteUrl());
    }


}
