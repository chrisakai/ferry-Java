package com.wlz.Interface;

import java.net.MalformedURLException;
import java.util.Map;

public interface MinioService {
    //解析地址
    Map parsePath(String picPath)  throws MalformedURLException;
    //从minio服务上获取图纸文件
    String minioGetPic(Map file);
}
