package com.beyond.entity;

public class ListItem {
    private String name;
    private String content;
    private String absPath;

    public ListItem() {
    }

    public ListItem(String name, String content, String absPath) {
        this.name = name;
        this.content = content;
        this.absPath = absPath;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
