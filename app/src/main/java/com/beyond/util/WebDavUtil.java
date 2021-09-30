package com.beyond.util;

import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2021/9/29
 */

public class WebDavUtil {

    public static boolean isAvailable(String rootUrl, String url, String username, String password) {
        Sardine sardine = new OkHttpSardine();
        sardine.setCredentials(username, password);
        try {
            String dirUrl = StringUtils.substringBeforeLast(url, "/");
            mkDir(rootUrl,dirUrl,sardine);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void mkDir(String rootUrl, String dirUrl,Sardine sardine) throws IOException {
        String parentUrl = StringUtils.substringBeforeLast(dirUrl, "/");
        if (!urlEquals(parentUrl, rootUrl)) {
            mkDir(rootUrl,parentUrl,sardine);
        }
        if (!sardine.exists(dirUrl)){
            sardine.createDirectory(dirUrl);
        }
    }

    public static boolean urlEquals(String url1, String url2) {
        return  StringUtils.equalsIgnoreCase(
                url1.replace("/", ""),
                url2.replace("/", ""));
    }

}
