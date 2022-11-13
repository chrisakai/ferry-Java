package com.wlz.controller;

import com.wlz.Interface.FerryService;
import com.wlz.Interface.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

@RestController
@RequestMapping("/ferry")
@Component
public class FerryRequestController {

    @Autowired
    private FerryService fs;
    @Autowired
    private MinioService minioService;

    @Value("${ftpUpperFolder}")
    String ftpUpperFolder;
    @Value("${localUploadFolder}")
    String localUploadFolder;
    @Value("${localDownloadFolder}")
    String localDownloadFolder;
    @Value("${ftpLowerFolder}")
    String ftpLowerFolder;

    @Value("${lower.host}")
    String lowerhost;
    @Value("${fixedftp.fixedPath}")
    String fixedPath;

    //向上层FTP服务器传送,下发动作
    @RequestMapping(value = "/toUpperBackup", method = RequestMethod.POST)
    public String upload2Upper(@RequestBody Map map) {
        Map minioMap = null;

        String picPath = ((String) map.get("fileName")).replace("localhost", lowerhost);
        System.out.println("图片地址为：" + picPath);

        try {
            minioMap = minioService.parsePath(picPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String fileName = minioService.minioGetPic(minioMap);
        String uploadResult = fs.upload(ftpUpperFolder, localUploadFolder + "/" + fileName);
        if (uploadResult == "upload success") {
            //删除应用服务器上的暂存图纸文件
            File file = new File(localUploadFolder + "/" + fileName);
            file.delete();
        }

        return "success";
    }

    //向指定FTP服务器传送,下发动作
    @RequestMapping(value = "/toUpper", method = RequestMethod.POST)
    public String upload2UpperFixedPath(@RequestBody Map map) {
        Map minioMap = null;

        String picPath = ((String) map.get("fileName"));
        String ip = ((String) map.get("ip"));
        int port = (int) map.get("port");
        String userName = ((String) map.get("userName"));
        String passWord = ((String) map.get("passWord"));
        System.out.println("图片地址为:" + picPath);
        System.out.println("ip: " + ip);
        System.out.println("port: " + port);
        System.out.println("userName: " + userName);
        System.out.println("passWord: " + passWord);

        //下载图纸后台指定ip地址下的FTP服务器中的指定文件至本地缓存文件夹
        String localBufferPath = fs.download(ip,port,userName,passWord,picPath);
        System.out.println("localBufferPath: " + localBufferPath);
        String uploadResult = fs.uploadFixed(fixedPath,localBufferPath);
        if (uploadResult == "upload success") {
            //删除应用服务器上的暂存图纸文件
            File file = new File(localBufferPath);
            file.delete();
        }

        return "success";
    }

    //向下层FTP服务器传送，下载动作
    @RequestMapping(value = "/picId", method = RequestMethod.POST)
    public String upload2Lower(@RequestParam int picId) {
        fs.upload(ftpLowerFolder, localUploadFolder);
        return "success";
    }

    //和服务器同机部署时可用（非图纸项目用）
    @RequestMapping(value = "/download")
    public String download() {
        fs.download(localDownloadFolder, ftpLowerFolder);
        return "success";
    }
}
