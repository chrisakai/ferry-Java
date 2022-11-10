package com.wlz.utils;

import com.sun.istack.internal.NotNull;

import com.wlz.entity.MinioItem;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Minio工具类
 *
 * @author Sariel
 * @apiNote
 * @since 2022/8/17 14:55
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {
    private final MinioClient minioClient;

    /**
     * bucket权限设置为custom
     * 可直接读取文件，即通过短链接下载文件
     */
    private final static String POLICY = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": [\n" +
            "                    \"*\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"Action\": [\n" +
            "                \"s3:GetBucketLocation\",\n" +
            "                \"s3:ListBucket\"\n" +
            "            ],\n" +
            "            \"Resource\": [\n" +
            "                \"arn:aws:s3:::%s\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": [\n" +
            "                    \"*\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"Action\": [\n" +
            "                \"s3:GetObject\"\n" +
            "            ],\n" +
            "            \"Resource\": [\n" +
            "                \"arn:aws:s3:::%s/*\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    /**
     * 判断bucket是否存在
     *
     * @param minioClient 初始化minio
     * @param bucketName  桶名称
     * @return boolean
     */
    public Boolean bucketExists(MinioClient minioClient, String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 判断本地minio服务器bucket是否存在
     *
     * @param bucketName 桶名称
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建存储bucket,本地
     *
     * @param bucketName 桶名称
     * @return boolean
     */
    public Boolean createBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            // 修改bucket权限为读写，默认只能写不能读。
//            minioClient.setBucketPolicy(new SetBucketPolicyArgs.Builder().config(String.format(POLICY, bucketName, bucketName)).bucket(bucketName).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建存储bucket
     *
     * @param minioClient 初始化minio
     * @param bucketName  桶名称
     * @return boolean
     */
    public Boolean createBucket(MinioClient minioClient, String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            // 修改bucket权限为读写，默认只能写不能读。
//            minioClient.setBucketPolicy(new SetBucketPolicyArgs.Builder().config(String.format(POLICY, bucketName, bucketName)).bucket(bucketName).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 文件名,除第一层”/“外的全路径，如”/img/a.jpg“
     * @return boolean
     */
    public Boolean checkFileIsExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 上传 bytes 通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param stream     数据流
     */
    public Boolean putInputStream(String bucketName, String objectName, InputStream stream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(stream, stream.available(), -1)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取所有文件
     *
     * @param bucketName bucketName
     */
    public List<MinioItem> list(String bucketName) throws Exception{
        try {
            List<MinioItem> list = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = URLDecoder.decode(item.objectName(), "utf-8");
                MinioItem minioItem = new MinioItem(item);
                minioItem.setFileType(FileTypeUtils.getTypeCode(objectName));
                minioItem.setObjectName(objectName);
                String url = URLDecoder.decode(getObjectUrl(bucketName, objectName), "utf-8");
                minioItem.setUrl(url);
                minioItem.setFilePath(url.substring(0, url.indexOf("?")));
                list.add(minioItem);
            }
            return list;
        } catch (Exception e) {
            log.error("获取所有文件失败：" + e);
            throw new Exception("获取所有文件失败：" + e);
        }
    }

    /**
     * 获取文件url
     *
     * @param objectName objectName
     * @return url
     */
    public String getObjectUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(2, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            log.error("获取文件url失败：" + e);
            return null;
        }
    }


    /**
     * 文件上传
     *
     * @param minioClient 初始化minio
     * @param file        文件
     * @param bucket      存储桶
     * @return Boolean
     */
    public Boolean upload(MinioClient minioClient, MultipartFile file, String bucket) {
        return uploadBoolean(minioClient, file, bucket);
    }

    @NotNull
    private Boolean uploadBoolean(MinioClient minioClient, MultipartFile file, String bucket) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException("文件读取失败");
        }
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucket).object(originalFilename)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件上传,本地
     *
     * @param file   文件
     * @param bucket 存储桶
     * @return Boolean
     */
    public Boolean upload(MultipartFile file, String bucket) {
        return uploadBoolean(minioClient, file, bucket);
    }

    /**
     * 文件下载,本地
     *
     * @param bucket    桶名称
     * @param fileName  文件名
     * @param localPath 本地存储地址
     * @return 下载结果
     */
    public boolean downloadFile(String bucket, String fileName, String localPath) {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .filename(localPath + "/" + fileName)
                            .build());
        } catch (Exception e) {
            log.error("---------- 资源文件{} 下载失败：{}", fileName, e.getMessage());
            return false;
        }
        return true;
    }
}
