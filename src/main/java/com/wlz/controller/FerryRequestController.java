package com.wlz.controller;

import com.wlz.Interface.FerryService;
import com.wlz.entity.FTPLower;
import com.wlz.entity.FTPUpper;
import com.wlz.entity.FixedAuditFTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/ferry")
@Component
public class FerryRequestController {

    @Autowired
    private FerryService fs;
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
    @Value("${fixedauditftp.fixedPath}")
    String fixedAuditPath;

    @Resource
    private FTPUpper ftpUpper;
    @Resource
    private FTPLower ftpLower;

    //向指定FTP服务器传送,下发动作
    @RequestMapping(value = "/toUpper", method = RequestMethod.POST)
    public String upload2UpperFixedPath(@RequestBody Map map) {

        String picPath = ((String) map.get("fileName"));
        String ip = ((String) map.get("ip"));
        int port = (int) map.get("port");
        String userName = ((String) map.get("userName"));
        String passWord = ((String) map.get("passWord"));
        boolean sync = (boolean) map.get("sync");
        System.out.println("图片地址为:" + picPath);
        System.out.println("ip: " + ip);
        System.out.println("port: " + port);
        System.out.println("userName: " + userName);
        System.out.println("passWord: " + passWord);
        System.out.println("是否sync: " + sync);

        //下载图纸后台指定ip地址下的FTP服务器中的指定文件至本地缓存文件夹
        String localBufferPath = fs.download(ip, port, userName, passWord, picPath);
        System.out.println("localBufferPath: " + localBufferPath);
        //上传至图纸服务器
        String uploadResult = fs.uploadFixed(fixedPath, localBufferPath);
        if (uploadResult == "upload success") {
            //删除应用服务器上的暂存图纸文件
            File file = new File(localBufferPath);
            file.delete();
        }
        //判断是否需要同步图片
        if (sync) {
            //上传至审计服务器
            String uploadAuditResult = fs.uploadAuditFixed(fixedAuditPath, localBufferPath);
            if (uploadAuditResult == "upload success") {
                //删除应用服务器上的暂存图纸文件
                File file = new File(localBufferPath);
                file.delete();
            }
        } else {
            return "no need to sync";
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

    /**
     * 文件上传
     */
    @RequestMapping(value = "/sendZip", method = RequestMethod.POST)
    public String sendZip(@RequestBody Map map) {
        String filePath = ((String) map.get("fileName"));
        boolean sync = (boolean) map.get("sync");
        System.out.println("文件地址为:" + filePath);
        System.out.println("是否sync: " + sync);
        String uploadResult = fs.upload(ftpUpper.getFixedPath(), filePath);
        //判断是否需要同步文件
//        if (sync) {
//            //上传至审计服务器
//            String uploadAuditResult = fs.uploadAuditFixed(fixedAuditFTP.getFixedPath(), filePath);
//        } else {
//            System.out.println("no need to sync");
//        }
        if (uploadResult == "upload success") {
            //删除应用服务器上的暂存图纸文件
            File file = new File(filePath);
            file.delete();
        }
        return "success";
    }

    /**
     * 文件下载
     */
    @RequestMapping(value = "/pullZip", method = RequestMethod.GET)
    public String pullZip() {
        fs.download(ftpLower.getHost(),ftpLower.getPort(),ftpLower.getUsername(),ftpLower.getPassword(),ftpLower.getFixedPath());
        return "success";
    }
}
