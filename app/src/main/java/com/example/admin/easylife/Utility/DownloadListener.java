package com.example.admin.easylife.Utility;

public interface DownloadListener {
    void onProgress(int progress);  //下载进度

    void onSuccess();               //下载成功

    void onFailed();                //下载失败

    void onPaused();                //下载暂停

    void onCanceled();              //下载取消
}
