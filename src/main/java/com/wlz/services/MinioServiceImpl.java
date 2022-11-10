package com.wlz.services;

import com.sun.jndi.toolkit.url.Uri;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MinioServiceImpl implements com.wlz.Interface.MinioService {

    @Value("${minio.accesskey}")
    private String accesskey;
    @Value("${minio.secretkey}")
    private String secretkey;
    @Value("${localUploadFolder}")
    String localUploadFolder;

    @Override
    public Map parsePath(String picPath) throws MalformedURLException{
        Map<String,String> map = new HashMap<>();
        Uri uri = null;
        try {
            uri = new Uri(picPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String path = uri.getPath();
        map.put("endpoint",picPath.substring(0, picPath.indexOf(uri.getPath())));
        map.put("bucket",path.substring(path.indexOf("/")+1,path.lastIndexOf("/")));
        map.put("fileName",path.substring(path.lastIndexOf("/")+1));
        return map;
    }

    @Override
    public String minioGetPic(Map file) {
        String endpoint = (String) file.get("endpoint");
        String bucket = (String) file.get("bucket");
        String fileName = (String) file.get("fileName");
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accesskey, secretkey)
                .build();
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .filename(localUploadFolder + "/" + fileName)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
