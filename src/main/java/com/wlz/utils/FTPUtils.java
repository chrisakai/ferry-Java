package com.wlz.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Slf4j
public class FTPUtils {
    public FTPClient ftpClient = new FTPClient();

    public static String LOCAL_CHARSET = "GBK";


    public FTPUtils() {
        // 设置将过程中使用到的命令输出到控制台
        this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }
    /**
     *
     * @todo连接到FTP服务器
     * @param hostname 主机名
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return 是否连接成功
     * @throws IOException
     *
     */
    public boolean connect(String hostname, int port, String username, String password) throws IOException {
        ftpClient.connect(hostname, port);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(username, password)) {
                // 设置PassiveMode传输
                ftpClient.enterLocalPassiveMode();
                // 设置以二进制流的方式传输
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                // 重要，每隔120S向控制端口发送心跳数据，保证控制端口的活性
                int defaultTimeOutSecond = 2*60*1000;

                /*    setDefaultTimeout
                 *（设置一个超时时间，用来当这个 Socket 调用了 read() 从 InputStream 输入流中
                 *    读取数据的过程中，如果线程进入了阻塞状态，那么这次阻塞的过程耗费的时间如果
                 *    超过了设置的超时时间，就会抛出一个 SocketTimeoutException 异常，但只是将
                 *    线程从读数据这个过程中断掉，并不影响 Socket 的后续使用。
                 *    如果超时时间为0，表示无限长。）
                 *  （注意，并不是读取输入流的整个过程的超时时间，而仅仅是每一次进入阻塞等待输入流中
                 *    有数据可读的超时时间）
                 */
                ftpClient.setDefaultTimeout(defaultTimeOutSecond);
                //设置默认超时时间setDefaultTimeout() 超时的工作跟 setSoTimeout() 是相同的，区别仅在于后者会覆盖掉前者设置的值。

                /**
                 * 使用指定的超时值将此套接字连接到服务器。
                 * 零超时被解释为无限超时。连接然后将阻塞，直到建立或发生错误
                 */
                ftpClient.setConnectTimeout(defaultTimeOutSecond);//设置连接超时时间

                /**
                 * 将此选项设置为非零
                 * 超时，调用此ServerSocket的accept（）
                 * 将仅在这段时间内阻塞。如果超时过期，
                 * 虽然ServerSocket仍然有效。必须启用该选项
                 * 在进入闭锁操作之前，应使其生效。这个超时必须是大于0
                 * 零超时被解释为无限超时。
                 */
                ftpClient.setDataTimeout(defaultTimeOutSecond);//设置读取数据超时时间（每次socket传输，不是指整个下载）

                this.ftpClient.setControlKeepAliveTimeout(15);

                // 设置控制端口发送心跳数据时控制端口的响应超时，得到回复结束后恢复原来时长
                this.ftpClient.setControlKeepAliveReplyTimeout(5000);
                if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                        "OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                    LOCAL_CHARSET = "UTF-8";
                }
                System.out.println("FTP编码为"+LOCAL_CHARSET);
                ftpClient.setControlEncoding(LOCAL_CHARSET);
                String root = ftpClient.printWorkingDirectory();
                if (!root.equals("/")){
                    System.out.println("FTP登录后的服务器根目录为不是/，请修改FTP根目录，不然导致上远程目录的绝对路径的都会报错");
                    return false;
                }else {
                    System.out.println("服务器根目录为/");
                }
                return true;
            }
        }
        disconnect();
        return false;
    }

    /**
     *
     * @todo连接到FTP服务器
     * @param hostname 主机名
     * @param port     端口
     * @return 是否连接成功
     * @throws IOException
     *
     */
    public boolean connect(String hostname, int port) throws IOException {
        ftpClient.connect(hostname, port);
//        ftpClient.setControlEncoding("GBK");
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            ftpClient.login("anonymous", "");
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 重要，每隔120S向控制端口发送心跳数据，保证控制端口的活性
            int defaultTimeOutSecond = 2*60*1000;

            /*    setDefaultTimeout
             *（设置一个超时时间，用来当这个 Socket 调用了 read() 从 InputStream 输入流中
             *    读取数据的过程中，如果线程进入了阻塞状态，那么这次阻塞的过程耗费的时间如果
             *    超过了设置的超时时间，就会抛出一个 SocketTimeoutException 异常，但只是将
             *    线程从读数据这个过程中断掉，并不影响 Socket 的后续使用。
             *    如果超时时间为0，表示无限长。）
             *  （注意，并不是读取输入流的整个过程的超时时间，而仅仅是每一次进入阻塞等待输入流中
             *    有数据可读的超时时间）
             */
            ftpClient.setDefaultTimeout(defaultTimeOutSecond);//设置默认超时时间

            /**
             * 使用指定的超时值将此套接字连接到服务器。
             * 零超时被解释为无限超时。连接然后将阻塞，直到建立或发生错误
             */
            ftpClient.setConnectTimeout(defaultTimeOutSecond);//设置连接超时时间

            /**
             * 将此选项设置为非零
             * 超时，调用此ServerSocket的accept（）
             * 将仅在这段时间内阻塞。如果超时过期，
             * 虽然ServerSocket仍然有效。必须启用该选项
             * 在进入闭锁操作之前，应使其生效。这个超时必须是大于0
             * 零超时被解释为无限超时。
             */
            ftpClient.setDataTimeout(defaultTimeOutSecond);//设置读取数据超时时间（每次socket传输，不是指整个下载）

            this.ftpClient.setControlKeepAliveTimeout(15);


            // 设置控制端口发送心跳数据时控制端口的响应超时，得到回复结束后恢复原来时长
            this.ftpClient.setControlKeepAliveReplyTimeout(5000);
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                    "OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                LOCAL_CHARSET = "UTF-8";
            }
            System.out.println("FTP编码为"+LOCAL_CHARSET);
            ftpClient.setControlEncoding(LOCAL_CHARSET);
            String root = ftpClient.printWorkingDirectory();
            if (!root.equals("/")){
                System.out.println("FTP登录后的服务器根目录为不是/，请修改FTP根目录，不然导致上远程目录的绝对路径的都会报错");
                return false;
            }else {
                System.out.println("服务器根目录为/");
            }

            return true;
        }

        disconnect();
        return false;
    }


    /**
     * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
     * @param remotePaths 远程文件或文件夹路径
     * @param local  本地目录路径
     * @return 上传的状态
     */
    public String download(String local, String... remotePaths) throws Throwable {
        if (StringUtils.isEmpty(local) || remotePaths == null || remotePaths.length==0){
            log.error("本地文件为空");
            return "本地文件为空";
        }

        // 检查远程文件是否存在
        for (String remote:remotePaths){
            remote = FTPPathToolkit.formatPathFTP(remote);
            FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
            if (files.length < 1) {
                System.out.println("远程文件不存在");
                return "Remote_File_Noexist";
            }
            File folder = new File(local);
            if (!folder.exists()) { // 如果本地文件夹不存在，则创建
                folder.mkdirs();
            }
            String result = "";
            if(getFileType(remote)==1){   //是目录
                result = downloadFolder(getURL(remote),local);
                System.out.println(result);
            }else {
                String localPath = null;
                localPath = local + "\\" + files[0].getName();
                localPath = FTPPathToolkit.formatPathFile(localPath);
                File file = null;   //本地文件  = new File(localPath)
                File[] listFiles = folder.listFiles();
                boolean flag = false;
                for (File fileTemp : listFiles) {
                    if (fileTemp.getName().equals(files[0].getName())) {
                        flag = true;
                        file = fileTemp;
                        break;
                    }
                }
                long lRemoteSize = files[0].getSize();
                // 本地存在文件，进行断点下载
                if (flag) {
                    long localSize = file.length();
                    // 判断本地文件大小是否大于远程文件大小
                    if (localSize >= lRemoteSize) {
                        System.out.println("本地文件大于远程文件，下载中止");
                        return "Local_Bigger_Remote";
                    }
                    result = downloadFile(remote, file, localSize, lRemoteSize); //断点续传
                    System.out.println(result);
                } else {
                    System.out.println("从0下载");
                    file = new File(localPath);
                    result = downloadFile(remote, file, 0L, lRemoteSize);
                    System.out.println(result);
                }
            }
        }
        return "OK";
    }

    public String downloadFolder(URL url,String localDir) throws Throwable {
        String path = url.getPath();
        File folder = new File(localDir + "/" + new File(path).getName());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String result = "";
        localDir = folder.getAbsolutePath();
        FTPFile[] ftpFiles = ftpClient.listFiles(new String(path.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
        String name = null;
        for (FTPFile file : ftpFiles) {
            name = file.getName();
            // 排除隐藏目录
            if (".".equals(name) || "..".equals(name)) {
                continue;
            }
            if (file.getType()==FTPFile.DIRECTORY_TYPE) { // 递归下载子目录
                result = downloadFolder(getURL(url, file.getName()), localDir);
            } else if (file.getType() == FTPFile.FILE_TYPE) { // 下载文件
                File localFolder = new File(localDir);
                File file1 = null;
                File[] localfiles = localFolder.listFiles();
                boolean flag=false;
                long lRemoteSize = file.getSize();
                for (File fileTemp:localfiles) {
                    if (fileTemp.getName().equals(file.getName())) {
                        flag = true;
                        file1 = fileTemp;
                        break;
                    }
                }
                if (flag){
                    System.out.println("断点续传");
                    long local_file_size=file1.length();
                    System.out.println("从"+local_file_size+"开始");
                    result =   downloadFile(FTPPathToolkit.formatPathFTP(url.getPath()+"/"+name),file1,local_file_size,lRemoteSize);
                }else {
                    System.out.println("从0下载");
                    file1 = new File(localDir + "/" + name);
                    result = downloadFile(FTPPathToolkit.formatPathFTP(url.getPath()+"/"+name),file1,0L,lRemoteSize);
                }

            }
        }
        return  result;
    }

    public String downloadFile(String remoteFile,File localFile,long localSize,long lRemoteSize) throws Throwable {
        if (localSize>0){
            OutputStream out = null;
            InputStream in = null;
            try{
                // 进行断点续传，并记录状态
                out = new FileOutputStream(localFile, true);
                ftpClient.setRestartOffset(localSize);
                in = ftpClient.retrieveFileStream(new String(remoteFile.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                byte[] bytes = new byte[1024];
                long step = lRemoteSize / 100;
                long process = localSize / step;
                int c;
                while ((c = in.read(bytes)) != -1) {
                    out.write(bytes, 0, c);
                    localSize += c;
                    long nowProcess = localSize / step;
                    if (nowProcess > process) {
                        process = nowProcess;
                        if (process % 10 == 0)
                            System.out.println("下载进度：" + process);
                        // TODO 更新文件下载进度,值存放在process变量中
                    }
                }
                in.close();
                out.close();
                boolean isDo = ftpClient.completePendingCommand();
                if (isDo) {
                    return  "Download_From_Break_Success";
                } else {
                    return  "Download_From_Break_Failed";
                }
            }catch (Throwable throwable){
                if (in!=null){
                    in.close();
                }
                if (out!=null){
                    out.close();
                }
                throw new Throwable();
            }
        }else {
            System.out.println(localFile.getPath());
            System.out.println(localFile.getName());
            OutputStream out = null;
            InputStream in = null;
            try {
                out = new FileOutputStream(localFile);
                in = ftpClient.retrieveFileStream(new String(remoteFile.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                byte[] bytes = new byte[1024];
                long step = lRemoteSize / 100;
                long process = 0;
                localSize = 0L;
                int c;
                while ((c = in.read(bytes)) != -1) {
                    out.write(bytes, 0, c);
                    localSize += c;
                    long nowProcess = localSize / step;
                    if (nowProcess > process) {
                        process = nowProcess;
                        if (process % 10 == 0)
                            System.out.println("下载进度：" + process);
                        // TODO 更新文件下载进度,值存放在process变量中
                    }
                }
                in.close();
                out.close();
                boolean upNewStatus = ftpClient.completePendingCommand();
                if (upNewStatus) {
                    return "Download_New_Success";
                } else {
                    return "Download_New_Failed";
                }
            }catch (Throwable throwable){
                if (in!=null){
                    in.close();
                }
                if (out!=null){
                    out.close();
                }
                throw new Throwable();
            }
        }
    }

    /**
     * 上传文件到FTP服务器，支持断点续传
     * @param localFiles  本地文件名称，绝对路径 file("/home/directory1/subdirectory/file.ext")
     * @param remote 远程文件路径，使用/home/directory1/subdirectory/    不加文件
     * 按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
     * @return 上传结果
     * @throws IOException
     */
    public void upload( String remote,File... localFiles) throws Throwable {
        if (StringUtils.isEmpty(remote) || localFiles == null || localFiles.length==0){
            log.error("本地文件为空");
            return;
        }
        remote = FTPPathToolkit.formatPathFTPIncludingSlash(remote);
        String result = "";

        mkdirs(remote); // 创建文件夹

        for (File file : localFiles) {
            if (file.isDirectory()) { // 上传目录
                uploadFolder(getURL(remote), file);
            }
            else{
                // 检查远程是否存在文件
                FTPFile[] files = ftpClient.listFiles(new String((remote+file.getName()).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                System.out.println("文件是"+Arrays.asList(files).toString());
                System.out.println("路径是"+new String((remote+file.getName()).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                if (files.length == 1) {
                    long remoteSize = files[0].getSize();
                    System.out.println(remoteSize+file.getPath());
                    long localSize = file.length();
                    if (remoteSize == localSize) {
                        System.out.println("不用上传已经有");
                        log.debug(file.getName()+" "+"上传成功");
                        continue;
                    } else if (remoteSize > localSize) {
                        log.debug(file.getName()+" "+"目标文件过大");
                        continue;
                    }
                    System.out.println(file.getName()+" "+"断点续传");
                    result = uploadFile(file.getName(), file, remoteSize);
                    log.debug(result+file.getPath());
                } else {
                    System.out.println(file.getName()+" "+"新的上传");
                    result = uploadFile(file.getName(), file, 0);
                    log.debug(result+file.getPath());
                }
                if (result.equals("Upload_From_Break_Failed") || result.equals("Upload_New_File_Failed")) {
                    log.debug(result+file.getPath());
                }
            }
        }
    }


    /**
     * 上传文件或目录
     *
     * @param dir
     *            目标文件
     *            是否删除源文件，默认为false
     *            文件或目录路径数组
     * @throws Exception
     */
    public void upload(String dir, String... paths)
            throws Throwable {
        if (dir==null || paths == null || paths.length ==0) {
            return;
        }
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        upload(dir,files);
    }

    public void uploadFolder(URL parentUrl,File file) throws Throwable {
        ftpClient.changeWorkingDirectory(new String(parentUrl.getPath().getBytes(LOCAL_CHARSET),StandardCharsets.ISO_8859_1));
        System.out.println("标记1"+parentUrl.getPath());
        String dir = file.getName(); // 当前目录名称
        URL url = getURL(parentUrl, dir);
        if (!exists( url.getPath())) { // 判断当前目录是否存在
            ftpClient.makeDirectory(new String(dir.getBytes(LOCAL_CHARSET),StandardCharsets.ISO_8859_1)); // 创建目录
            System.out.println("标记3");
        }
        ftpClient.changeWorkingDirectory(new String(dir.getBytes(LOCAL_CHARSET),StandardCharsets.ISO_8859_1));
        System.out.println("标记2"+dir);
        File[] files = file.listFiles(); // 获取当前文件夹所有文件及目录
        for (int i = 0; i < files.length; i++) {
            file = files[i];
            if (file.isDirectory()) { // 如果是目录，则递归上传
                uploadFolder(url, file);
            } else { // 如果是文件，直接上传
//                client.changeDirectory(url.getPath());
                // 检查远程是否存在文件
                String result;
                FTPFile[] ftpFiles = ftpClient.listFiles(new String((url.getPath()+"/"+file.getName()).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                System.out.println("文件是"+Arrays.asList(ftpFiles).toString());
                System.out.println("路径是"+new String((url.getPath()+"/"+file.getName()).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));
                if (ftpFiles.length == 1) {
                    System.out.println();
                    long remoteSize = ftpFiles[0].getSize();
                    long localSize = file.length();
                    if (remoteSize == localSize) {
                        System.out.println(file.getName()+" "+"不用上传已经有");
                        log.debug(file.getName()+" "+"上传成功");
                        continue;
                    } else if (remoteSize > localSize) {
                        log.debug(file.getName()+" "+"目标文件过大");
                        continue;
                    }
                    System.out.println(file.getName()+" "+"断点续传");
                    result = uploadFile(file.getName(), file, remoteSize);
                    log.debug(result+file.getPath());
                } else {
                    System.out.println(file.getName()+" "+"新的上传");
                    result = uploadFile(file.getName(), file, 0);
                    log.debug(result+file.getPath());
                }
                if (result == "Upload_From_Break_Failed" || result == "Upload_New_File_Failed") {
                    log.debug(result+file.getPath());

                }




            }
        }
    }

    /**
     * 判断文件或目录是否存在
     *
     *            FTP客户端对象
     * @param dir
     *            文件或目录
     * @return
     * @throws Exception
     */
    private boolean exists(String dir) throws Exception {
        return getFileType(dir) != -1;

    }

    /**
     * 判断当前为文件还是目录
     *
     *            FTP客户端对象
     * @param dir
     *            文件或目录
     * @return -1、文件或目录不存在 0、文件 1、目录
     */
    private int getFileType( String dir) {
        FTPFile[] files = null;
        try {
            files = ftpClient.listFiles(new String((dir).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));  //不存在目录也不报错
        } catch (Exception e) {
            return -1;
        }
        if (files.length > 1) {
            return FTPFile.DIRECTORY_TYPE;
        } else if (files.length == 1) {
            FTPFile f = files[0];
            if (f.getType() == FTPFile.DIRECTORY_TYPE) {
                return FTPFile.DIRECTORY_TYPE;
            }
            String path = FTPPathToolkit.formatPathFTP(dir) + "/" + f.getName();
            try {
                int len = ftpClient.listFiles(new String((path).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1)).length;
                if (len == 1) {
                    return FTPFile.DIRECTORY_TYPE;
                } else {
                    return FTPFile.FILE_TYPE;
                }
            } catch (Exception e) {
                return FTPFile.FILE_TYPE;
            }
        } else {
            try {
                if (!ftpClient.changeWorkingDirectory(new String((dir).getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1))){
                    return -1;
                }
                ftpClient.changeToParentDirectory();
                return FTPFile.DIRECTORY_TYPE;
            } catch (Exception e) {
                return -1;
            }
        }
    }

    /**
     * 断开与远程服务器的连接
     * @throws IOException
     *
     */
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
            System.out.println("关闭连接");
        }
    }
    /**
     *
     * 递归创建远程服务器目录
     * @param remote    远程服务器文件绝对路径
     * @return 目录创建是否成功
     * @throws IOException
     *
     */
    public String CreateDirecroty(String remote) throws IOException {
        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(new String(directory.getBytes(LOCAL_CHARSET), "iso-8859-1"))) {
            // 如果远程目录不存在，则递归创建远程服务器目录
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = new String(remote.substring(start, end).getBytes(LOCAL_CHARSET), "iso-8859-1");
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        System.out.println("创建目录失败");
                        return "Create_Directory_Fail";
                    }
                }
                start = end + 1;
                end = directory.indexOf("/", start);
                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return "Create_Directory_Success";
    }

    /**
     * 创建目录
     *            FTP客户端对象
     * @param dir
     *            目录绝对路径
     * @throws Exception
     */
    private void mkdirs(String dir) throws Exception {
        if (dir == null || dir.equals("")) {
            log.error("目标位置为空");
            return;
        }
        dir = FTPPathToolkit.formatPathFTP(dir);
//        dir = dir.replace("//", "/");
        String[] dirs = dir.split("/"); //如果/开头，会有一个空字符为数组索引0
        String path = "";
        for (int i = 0; i < dirs.length; i++) {
            path  +=  dirs[i] + "/";      //第一个dirs[0] 会是空  则 第一个path 为 /
            if (!isDirExist(path)) {
                if (!path.equals("/")){   //如果不是根目录
                    if (!ftpClient.makeDirectory(new String(dirs[i].getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1))){
                        System.out.println("目录创建失败");
                        throw new Exception();
                    }}
                System.out.println("创建目录"+path);
                ftpClient.changeWorkingDirectory(new String(path.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));// 进入创建的目录
            }
        }
    }

    //检查目录是否存在
    private boolean isDirExist(String dir) {
        try {
            if (!ftpClient.changeWorkingDirectory(new String(dir.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1))){
                return false;
            }
            System.out.println("切换目录"+dir);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     *
     * 上传文件到服务器,新上传和断点续传
     * @param remoteFile  远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile   本地文件File句柄，绝对路径
     * @return
     * @throws IOException
     *
     */
    public String uploadFile(String remoteFile, File localFile,  long remoteSize)
            throws Throwable {
        RandomAccessFile raf = null;
        OutputStream out = null;
        try {
            String status = "";
            // 显示进度的上传
            long step = localFile.length() / 100;
            long process = 0;
            long localreadbytes = 0L;
            raf = new RandomAccessFile(localFile, "r");
            out = ftpClient.appendFileStream(new String(remoteFile.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));/*追加续传*/
//         out = ftpClient.storeFileStream(new String(remoteFile.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1));/*重置为0开始传，覆盖，覆写*/
            // 断点续传
            if (remoteSize > 0) {
                System.out.println(localFile.getName()+" "+"续传！"+remoteSize);
                ftpClient.setRestartOffset(remoteSize);
                process = remoteSize / step;
                raf.seek(remoteSize);
                localreadbytes = remoteSize;
            }
            byte[] bytes = new byte[1024];
            int c;
            while ((c = raf.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localreadbytes += c;
//            if (localreadbytes / step != process) {
//                process = localreadbytes / step;
//                System.out.println("上传进度:" + process);
//                // TODO 汇报上传状态
//            }
            }
            out.flush();
            raf.close();
            out.close();
            boolean result = ftpClient.completePendingCommand();
            if (remoteSize > 0) {
                status = result ? "Upload_From_Break_Success" : "Upload_From_Break_Failed";
            } else {
                status = result ? "Upload_New_File_Success" : "Upload_New_File_Failed";
            }

            return status;
        }catch (Throwable throwable) {  //如果上传过程中断网，连接还在无法删除该文件，而且容易占资源不放。
            if (out!=null) {
                out.flush();
                out.close();
            }
            if (raf!=null) {
                raf.close();
            }
            ftpClient.disconnect();
            System.out.println("出异常关闭流"); //如果成功记得把下载也弄弄
            throwable.printStackTrace();
//            return "出异常关闭流 Failed";
            throw new Throwable();
        }
    }

    /**
     * 获取FTP目录
     *
     * @param url
     *            原FTP目录
     * @param dir
     *            目录
     * @return
     * @throws Exception
     */
    private URL getURL(URL url, String dir) throws Exception {
        String path = url.getPath();
        if (!path.endsWith("/") && !path.endsWith("//")) {
            path += "/";
        }
        dir = dir.replace("//", "/");
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        path += dir;
        return new URL(url, path);
    }
    /**
     * 获取FTP目录
     *
     * @param dir
     *            目录
     * @return
     * @throws Exception
     */
    private URL getURL(String dir) throws Exception {
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        return getURL(new URL("http://8.8.8.8"), dir);
    }

    /**
     * 测试方法
     * @author: tompai
     * @createTime: 2019年12月24日 下午11:08:24
     * @history:
     * @param args void
     */
    public static void main(String[] args) {
        FTPUtils myFtp = new FTPUtils();
        try {
//            Ftp ftp = new Ftp();
            System.out.println("编码为"+FTPUtils.LOCAL_CHARSET);
            System.err.println(myFtp.connect("221.224.163.15", 21, "FIH", "TqyjaGLxfGzj%AVE"));
//            System.err.println(myFtp.connect("10.132.240.89", 21, "it", "123456"));
//            projectName=new String(projectName.getBytes("UTF-8"),"iso-8859-1");
            System.out.println("编码为"+FTPUtils.LOCAL_CHARSET);
//            myFtp.upload("D:/3/测试.txt","/smart/测试.txt");
//            System.out.println(myFtp.ftpClient.printWorkingDirectory());
//            System.out.println(myFtp.ftpClient.changeWorkingDirectory(FTPUtils.+"/log"));
//            myFtp.ftpClient.changeWorkingDirectory(new String("DebugLog".getBytes(FTPUtils.LOCAL_CHARSET),"iso-8859-1"));
//            System.out.println(Arrays.toString(myFtp.ftpClient.listFiles(+"/log/DebugLog/")));
//            System.out.println(myFtp.ftpClient.changeWorkingDirectory(FTPUtils.+"/smart"));
//            System.out.println(myFtp.ftpClient.makeDirectory());
//            System.out.println(Arrays.toString(myFtp.ftpClient.listFiles(+"/smart/202008/202008241228512c103adcaf484989b6126168be788915.pdf")));
//            System.out.println(Arrays.toString(myFtp.ftpClient.listFiles(+"/smart/202008/")));
//            System.out.println(Arrays.toString(myFtp.ftpClient.listFiles(+"/smaasdasrt/20asdasd2008/")));
//            myFtp.ftpClient.changeWorkingDirectory(new String("权利的游戏".getBytes("GBK"),"iso-8859-1"));
//             myFtp.upload("/sample/", "D:\\3\\LOGC.zip");
            System.out.println(myFtp.download( "D:/5-1/","/sample/测试"));
            myFtp.disconnect();
        } catch (IOException e) {
            System.out.println("连接FTP出错：" + e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}


