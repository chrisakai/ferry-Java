package com.wlz.services;

import com.wlz.entity.FTPLower;
import com.wlz.entity.FTPUpper;
import com.wlz.utils.FTPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class FerryServiceImpl implements com.wlz.Interface.FerryService {

    @Resource
    private FTPUpper ftpUpper;

    @Resource
    private FTPLower ftpLower;

    @Autowired
    private FTPUtils ftpUtils;

    @Override
    public String upload(String toFtpFile, String fromFile) {
        try {
            ftpUtils.connect(ftpUpper.getHost(), ftpUpper.getPort(), ftpUpper.getUsername(), ftpUpper.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp connect failed";
        }
        try {
            ftpUtils.upload(toFtpFile,fromFile);   //上传文件字符串
        } catch (Throwable e) {
            e.printStackTrace();
            return "upload failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        return "upload success";
    }

    @Override
    public String upload(String toFtpFile, String[] fromFiles) {
        FTPUtils ftpUtils = new FTPUtils();
        try {
            ftpUtils.connect(ftpUpper.getHost(), ftpUpper.getPort(), ftpUpper.getUsername(), ftpUpper.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp connect failed";
        }
//        toFtpFile = "\\FTPDestination\\";
//        fromFiles = new String[2];
//        fromFiles[0] = "C:\\chcFTPTest\\FTPStart\\";
//        fromFiles[1] = "C:\\chcFTPTest\\00项目验收资料清单.xlsx";
        try {
            ftpUtils.upload(toFtpFile,fromFiles);   //上传文件字符串
        } catch (Throwable e) {
            e.printStackTrace();
            return "upload failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        return "upload success";
    }

    @Override
    public String download(String toFile, String FromFtpFile) {
        FTPUtils ftpUtils = new FTPUtils();
        try {
            ftpUtils.connect(ftpUpper.getHost(), ftpUpper.getPort(), ftpUpper.getUsername(), ftpUpper.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp connect failed";
        }
        try {
            ftpUtils.download(toFile,FromFtpFile);   //上传文件字符串
        } catch (Throwable e) {
            e.printStackTrace();
            return "upload failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        return "upload success";
    }

    @Override
    public String download(String toFile, String[] FromFtpFiles) {
        FTPUtils ftpUtils = new FTPUtils();
        try {
            ftpUtils.connect(ftpUpper.getHost(), ftpUpper.getPort(), ftpUpper.getUsername(), ftpUpper.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp connect failed";
        }
//        toFile = "\\FTPDestination\\";
//        FromFtpFiles = new String[2];
//        FromFtpFiles[0] = "C:\\chcFTPTest\\FTPStart\\";
//        FromFtpFiles[1] = "C:\\chcFTPTest\\00项目验收资料清单.xlsx";
        try {
            ftpUtils.upload(toFile,FromFtpFiles);   //上传文件字符串
        } catch (Throwable e) {
            e.printStackTrace();
            return "upload failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        return "upload success";
    }
}
