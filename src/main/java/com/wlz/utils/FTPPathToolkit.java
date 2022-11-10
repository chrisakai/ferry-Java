package com.wlz.utils;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FTPPathToolkit {

    /**
     * <p>Title: </p>
     * <p>Description: </p>
     */
    public FTPPathToolkit() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     *
     * @Title: formatPath4File
     * @Description:格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的文件路径分隔符。
     * 本方法操作系统自适应
     * @param @param path
     * @param @return
     * @return String
     * @throws
     */
    public static String formatPathFile(String path) {
        String reg0 = "\\\\+";
        String reg = "\\\\+|/+";
        String temp = path.trim().replaceAll(reg0, "/");
        temp = temp.replaceAll(reg, "/");
        if (temp.length() > 1 && temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        temp = temp.replace('/', File.separatorChar);
        return temp;
    }
    /**
     *
     * @Title: formatPathFTP
     * @Description: 格式化文件路径，将其中不规范的分隔转换为标准的分隔符 ,
     * 并且去掉末尾的"/"符号
     * @param @param path
     * @param @return
     * @return String
     * @throws
     */
    public static String formatPathFTP(String path) {
        String reg0 = "\\\\+";
        String reg = "\\\\+|/+";
        String temp = path.trim().replaceAll(reg0, "/");
        temp = temp.replaceAll(reg, "/");
        if (temp.length() > 1 && temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp;
    }

    /**
     *
     * @Title: formatPathFTP
     * @Description: 格式化文件路径，将其中不规范的分隔转换为标准的分隔符 ,
     * 添加末尾的"/"符号
     * @param @param path
     * @param @return
     * @return String
     * @throws
     */
    public static String formatPathFTPIncludingSlash(String path) {
        String reg0 = "\\\\+";
        String reg = "\\\\+|/+";
        String temp = path.trim().replaceAll(reg0, "/");
        temp = temp.replaceAll(reg, "/");
        if (temp.length() > 1 && temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        temp = temp+"/";
        return temp;
    }
    /**
     *
     * @Title: genParentPath4FTP
     * @Description: 获取FTP路径的父路径，但不对路径有效性做检查
     * @param @param path
     * @param @return
     * @return String
     * @throws
     */
    public static String genParentPathFTP(String path) {
        String parentPath = new File(path).getParent();
        if (parentPath == null) return null;
        else return formatPathFTP(parentPath);
    }

    public static void main(String[] args) {
        String path = "//git//handwritten-tools";
        System.out.println(FTPPathToolkit.formatPathFTPIncludingSlash(path));
        String path1 = "//git//handwritten-tools//";
        System.out.println(FTPPathToolkit.formatPathFTPIncludingSlash(path1));
        String path2 = "//git//handwritten-tools//";
        System.out.println(FTPPathToolkit.formatPathFTPIncludingSlash(path2));
    }
}
