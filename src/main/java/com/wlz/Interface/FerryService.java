package com.wlz.Interface;

public interface FerryService {
    //上传文件、文件夹字符串
    String upload(String toFtpFile,String fromFile);
    //上传文件类型数组
    String upload(String toFtpFile,String[] fromFiles);
    //下载文件、文件夹字符串
    String download(String toFile,String FromFtpFile);
    //下载文件类型数组
    String download(String toFile,String[] FromFtpFiles);
}
