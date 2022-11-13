package com.wlz.services;

import com.wlz.entity.FTPLower;
import com.wlz.entity.FTPUpper;
import com.wlz.entity.FixedFTP;
import com.wlz.utils.FTPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class FerryServiceImpl implements com.wlz.Interface.FerryService {

    @Resource
    private FTPUpper ftpUpper;

    @Resource
    private FTPLower ftpLower;

    @Resource
    private FixedFTP fixedFTP;

    @Autowired
    private FTPUtils ftpUtils;

    @Value("${localUploadFolder}")
    String localUploadFolder;

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
    public String uploadFixed(String toFtpFile, String fromFile) {
        try {
            ftpUtils.connect(fixedFTP.getHost(), fixedFTP.getPort(), fixedFTP.getUsername(), fixedFTP.getPassword());
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
            return "download failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        return "download success";
    }

    /**
     *下载图纸后台指定ip地址下的FTP服务器中的指定文件至本地缓存文件夹
     * @param ip
     * @param port
     * @param userName
     * @param passWord
     * @param fileName
     * @return
     */
    @Override
    public String download(String ip, int port, String userName, String passWord, String fileName) {
        try {
            ftpUtils.connect(ip, port, userName, passWord);
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp connect failed";
        }
        try {
            //下载FTP相对路径下的文件fileName至本地缓存文件夹
            ftpUtils.download(localUploadFolder,fileName);
        } catch (Throwable e) {
            e.printStackTrace();
            return "download failed";
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return "ftp disconnect failed";
        }
        int dex = fileName.lastIndexOf("/");
        System.out.println("本地文件路径为：" + fileName);
        System.out.println("本地文件路径最后符号\\位置为：" + dex);
        String filename = fileName.substring(dex+1);
        System.out.println("本地文件名为：" + filename);
        return localUploadFolder.concat(filename);
    }

    @Override
    public String download(String toFile, String[] FromFtpFiles) {
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
