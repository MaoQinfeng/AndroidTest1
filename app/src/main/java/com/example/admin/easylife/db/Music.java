package com.example.admin.easylife.db;

import org.litepal.crud.LitePalSupport;

public class Music extends LitePalSupport {
    private String name;
    private String url;
    private long len;

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
