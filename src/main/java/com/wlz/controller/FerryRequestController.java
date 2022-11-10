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


    //向上层FTP服务器传送,下发动作
    @RequestMapping(value = "/toUpper", method = RequestMethod.POST)
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
