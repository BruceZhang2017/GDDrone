package com.jieli.stream.dv.gdxxx.util;

import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import com.jieli.stream.dv.gdxxx.ui.a;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class FTPClientUtil implements IConstant{

    private String tag = "FTPClientUtil";
    private static FTPClientUtil instance;
    private FTPClient mFTPClient;
    private String currentFTPPath;

    public FTPClientUtil(){
        mFTPClient = new FTPClient();
    }

    public FTPClient getFTPClient(){
        if(mFTPClient == null){
            mFTPClient = new FTPClient();
        }
        return mFTPClient;
    }

    public static FTPClientUtil getInstance() {
        if(instance == null){
            synchronized (FTPClientUtil.class){
                if(instance == null){
                    instance = new FTPClientUtil();
                }
            }
        }
        return instance;
    }

    /**
     * connect FTP
     * @param addr        FTP 地址
     * @param port        端口号（默认为21）
     * @param hostName    账号名
     * @param password    密码
     * @param changePath  子目录路径
     */
    public boolean connectAndLoginFTP(String addr, int port, String hostName, String password, String changePath) {
        if (TextUtils.isEmpty(addr) || TextUtils.isEmpty(hostName) || TextUtils.isEmpty(password)) {
            Dbug.e(tag, "-connectAndLoginFTP- parameter is empty!");
            return false;
        }
        try {
            if(mFTPClient != null){
                mFTPClient.setDefaultPort(port);
                mFTPClient.setDataTimeout(40000);
                mFTPClient.setConnectTimeout(20000);
                mFTPClient.connect(addr);
                if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                    if (mFTPClient.login(hostName, password)) {
                        mFTPClient.setControlEncoding("UTF-8");
                        mFTPClient.enterLocalPassiveMode();
                        currentFTPPath = mFTPClient.printWorkingDirectory();
                        Dbug.w(tag, "connect Ftp server success, root Path : " + currentFTPPath);
                        if (!TextUtils.isEmpty(changePath)) {
                            if(checkExistPath(changePath)) {
                                String path = currentFTPPath + changePath;
                                if (mFTPClient.changeWorkingDirectory(path)) {
                                    Dbug.e(tag, "connect Ftp server success!  currentFTPPath : " + path);
                                    currentFTPPath = path;
                                    return true;
                                }
                            }else{
                                Dbug.e(tag, "The path does not exist in the ftp server, changePath : " + changePath);
                            }
                        }
                        if (TextUtils.isEmpty(changePath)){
                            Dbug.e(tag, "connect Ftp server success!");
                            return true;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Dbug.e(tag, "connectFTP SocketException ===> " + e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            Dbug.e(tag, "connectFTP IOException ===> " + e.getMessage());
            //e.printStackTrace();
        }
        disconnect();
        return false;
    }

    public String getCurrentFTPPath(){
        return currentFTPPath;
    }

    /**
     * change ftp server work path
     */
    public boolean changeWorkPath(String path) {
        if (TextUtils.isEmpty(path)) {
            Dbug.e(tag, "-connectAndLoginFTP- parameter is empty!");
            return false;
        }
        if (mFTPClient != null && mFTPClient.isConnected()) {
            try {
                boolean result = mFTPClient.changeWorkingDirectory(path);
                if(result){
                    currentFTPPath = path;
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 检查子路径是否存在
     * @param subPath  目标子路径
     */
    private boolean checkExistPath(String subPath){
        boolean result = false;
        if(!TextUtils.isEmpty(subPath)){
            String[] paths;
            if(subPath.contains("/")){
                paths = subPath.split("/");
            }else{
                paths = new String[]{subPath};
            }
            String mPath = "";
            for (String path : paths){
                if(!TextUtils.isEmpty(path)){
                    try{
                        if (mFTPClient == null)
                            return false;
                        FTPFile[] dirs = mFTPClient.listDirectories(("/" + mPath));
                        if(dirs != null){
                            for (FTPFile file : dirs){
                                if(file.isDirectory() && path.equals(file.getName())){
                                    if(TextUtils.isEmpty(mPath)){
                                        mPath = path;
                                    }else{
                                        mPath += "/" + path;
                                    }
                                    break;
                                }
                            }
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            result = (subPath.equals(mPath));
        }
        return result;
    }


    /**
     * disconnect FTP
     */
    public void disconnect() {
        if (mFTPClient != null && mFTPClient.isConnected()) {
            try {
                mFTPClient.logout();
                mFTPClient.disconnect();
            } catch (IOException e) {
                Dbug.e(tag, "disconnect IOException --> " + e.getMessage());
                e.printStackTrace();
            }finally {
                currentFTPPath = null;
            }
        }
    }

    private static int failedNum;
    /**
     * 下载FTP索引文档
     * @param platforms    下载路径
     * @param isOk         下载结果
     */
    public void downloadTxt(List<String> platforms, boolean isOk){
        if(null == platforms || platforms.size() == 0){
            Dbug.e(tag, " downloadTxt parameters is empty!");
            return;
        }
        if(isOk){
            platforms.remove(0);
            failedNum = 0;
        }else{
            failedNum++;
            if(failedNum > 2){
                failedNum = 0;
                platforms.remove(0);
            }
        }
        if(platforms.size() > 0){
            String changePath = platforms.get(0);
            if(TextUtils.isEmpty(changePath)){
                Dbug.e(tag, " downloadTxt changePath is empty!");
                return;
            }
            String versionDir = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), VERSION, null, null);
            FileOutputStream outputStream = null;
            InputStream inputStream = null;
            int length;
            byte[] buffer = new byte[44 *1460];
            try{
                if(mFTPClient == null){
                    mFTPClient = new FTPClient();
                }
                if(connectAndLoginFTP(FTP_HOST_NAME, DEFAULT_FTP_PORT, FTP_USER_NAME, FTP_PASSWORD, changePath)){
                    String[] filesName = mFTPClient.listNames();
                    boolean isSameValue = false;
                    if(filesName != null && filesName.length > 0){
                        for (String name : filesName){
                            Dbug.i(tag, " ftp list name : " + name);
                            if(VERSION_JSON.equals(name)){
                                isSameValue = true;
                                break;
                            }
                        }
                        if(isSameValue){
                            try{
                                String outPath = versionDir + File.separator + changePath + "_" + VERSION_JSON;
                                outputStream = new FileOutputStream(outPath);
                            }catch (IOException e){
                                e.printStackTrace();
                                disconnect();
                                downloadTxt(platforms, false);
                                return;
                            }
                            inputStream = mFTPClient.retrieveFileStream(VERSION_JSON);
                            if(inputStream == null){
                                Dbug.e(tag, "downloadTxt inputStream is empty !");
                                disconnect();
                                downloadTxt(platforms, false);
                                return;
                            }
                            while ((length = inputStream.read(buffer)) != -1){
                                outputStream.write(buffer, 0, length);
                            }
                            boolean result = mFTPClient.completePendingCommand();
                            if(result){
                                Dbug.e(tag, " download VERSION_JSON success");
                            }else{
                                Dbug.e(tag, " download VERSION_JSON failed");
                            }
                            disconnect();
                            downloadTxt(platforms, result);
                        }else{
                            downloadTxt(platforms, false);
                        }
                    }else{
                        Dbug.e(tag, "filesName == null!");
                        downloadTxt(platforms, false);
                    }
                }else{
                    Dbug.e(tag, " connectAndLoginFTP failed!");
                    downloadTxt(platforms, false);
                }
            }catch (IOException e){
                Dbug.e(tag, "downloadTxt IOException : " +e.getMessage());
                e.printStackTrace();
            }finally {
                disconnect();
                try{
                    if(inputStream != null){
                        inputStream.close();
                    }
                    if(outputStream != null){
                        outputStream.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 下载升级文件
     * @param remotePath  服务器地址
     * @param updateType  升级类型（APK, SDK）
     * @param fileType    文件类型（1：描述文件 2： 升级文件）
     * @param handler     UI管理通知
     */
    public  List<String> downLoadUpdateFile(String remotePath, int updateType, int fileType, Handler handler){
        if(TextUtils.isEmpty(remotePath)){
            Dbug.e(tag, "filename, localPath is null!");
            return null;
        }
        FileOutputStream outUpdateFileStream = null;
        InputStream inputUpdateFileStream = null;
        String remoteFilePath = null;
        String saveFilePath = null;
        List<String> updatePaths = new ArrayList<>();
        try {
            if (connectAndLoginFTP(FTP_HOST_NAME, DEFAULT_FTP_PORT, FTP_USER_NAME, FTP_PASSWORD, remotePath)) {
                String[] ftpFilesNames = mFTPClient.listNames();
                if (ftpFilesNames == null || ftpFilesNames.length == 0) {
                    disconnect();
                    return null;
                }
                String updateFileName = null;
                if(fileType == FILE_DESC_TXT){
                    for (String filename : ftpFilesNames){
                        if(TextUtils.isEmpty(filename)) continue;
                        Dbug.w(tag, "filename --> " +filename);
                        if(filename.contains(".txt") || filename.contains(".TXT")){
                            updateFileName = filename;
                            break;
                        }
                    }
                }else{
                    for (String filename : ftpFilesNames){
                        if(TextUtils.isEmpty(filename)) continue;
                        Dbug.w(tag, "filename --> " +filename);
                        if(updateType == UPGRADE_APK_TYPE){
                            if(filename.contains(".apk") || filename.contains(".APK")){
                                updateFileName = filename;
                                break;
                            }
                        }else if(updateType == UPGRADE_SDK_TYPE){
                            if(filename.contains(".bfu") || filename.contains(".BFU")) {
                                updateFileName = filename;
                                break;
                            }
                        }
                    }
                }
                if(TextUtils.isEmpty(updateFileName)){
                    return null;
                }
                Dbug.i(tag, "-downLoadUpdateFile- updateFileName : " + updateFileName);
                String localUpdatePath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), UPGRADE, null, null);
                remoteFilePath = File.separator + remotePath + File.separator + updateFileName;
                saveFilePath = localUpdatePath + File.separator + updateFileName;
                File saveFile = new File(saveFilePath);
                if (saveFile.exists() && saveFile.isFile()) {
                    if (saveFile.delete()) {
                        Dbug.w(tag, "delete exists update file !");
                    }
                }
                try {
                    outUpdateFileStream = new FileOutputStream(saveFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                    return null;
                }
                int length;
                byte[] buffer = new byte[44 * 1460];
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                long downloadFileSize = 0;
                FTPFile[] remoteFiles = mFTPClient.listFiles();
                if(remoteFiles != null){
                    Dbug.i(tag, "-downLoadUpdateFile- remoteFiles size : " + remoteFiles.length);
                    for (FTPFile file : remoteFiles){
                        Dbug.i(tag, "-downLoadUpdateFile- file name : " + file.getName());
                        if(file.getName().equals(updateFileName)) {
                            Dbug.w(tag, "-downLoadUpdateFile- download File size : " + file.getSize());
                            downloadFileSize = file.getSize();
                            break;
                        }
                    }
                }else{
                    Dbug.e(tag, "-downLoadUpdateFile- remoteFile is null!");
                    disconnect();
                    return null;
                }
                inputUpdateFileStream = mFTPClient.retrieveFileStream(updateFileName);
                if (inputUpdateFileStream == null) {
                    Dbug.e(tag, "-downLoadUpdateFile- inputUpdateFileStream is empty !");
                    disconnect();
                    return null;
                }
                int size = 0;
                while ((length = inputUpdateFileStream.read(buffer)) != -1) {
                    outUpdateFileStream.write(buffer, 0, length);
                    size += length;
                    if(downloadFileSize > 0){
                        int progress = (int)(size * 100 / downloadFileSize);
                        if( progress % 2 == 0){
                            if(fileType == FILE_TYPE_UPGRADE){
                                if(handler != null){
                                    handler.sendMessage(handler.obtainMessage(MSG_UPDATE_DOWNLOAD_PROGRESS, progress, 0));
                                }
                            }
                        }
                    }
                }
                if(mFTPClient.completePendingCommand()){
                    Dbug.w(tag, "-downLoadUpdateFile- download File success");
                }else{
                    Dbug.w(tag, "-downLoadUpdateFile- download File failed");
                    remoteFilePath = null;
                }
            }else{
                Dbug.e(tag, "login ftp server failed!");
                remoteFilePath = null;
            }
        }catch (IOException e){
            e.printStackTrace();
            remoteFilePath = null;
        }finally {
            disconnect();
            try{
                if(inputUpdateFileStream != null){
                    inputUpdateFileStream.close();
                }
                if(outUpdateFileStream != null){
                    outUpdateFileStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if(!TextUtils.isEmpty(remoteFilePath) && !TextUtils.isEmpty(saveFilePath)){
            updatePaths.add(remoteFilePath);  //ftp url
            updatePaths.add(saveFilePath);    //save file path
        }

        return updatePaths;
    }

    /**
     * 上传文件
     * @param remotePathName   服务器地址
     * @param localFilePath    本地文件地址
     * @param handler          UI管理
     * @return                 结果
     */
    public boolean uploadFile(String remotePathName, String localFilePath, Handler handler){
        InputStream inputStream = null;
        OutputStream outputStream = null;
        long localFileSize = 0;
        String ip = ClientManager.getClient().getAddress();
        if(connectAndLoginFTP((TextUtils.isEmpty(ip) ? DEFAULT_DEV_IP : ip), DEFAULT_FTP_PORT, INSIDE_FTP_USER_NAME, INSIDE_FTP_PASSWORD, null)){
            int length;
            int size = 0;
            int progress = 0;
            try {
                try {
                    File localFile = new File(localFilePath);
                    if(localFile.exists()){
                        inputStream = new FileInputStream(localFile);
                        localFileSize = localFile.length();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Dbug.e(tag, "-uploadFile- connect ftp success, localFileSize = " +localFileSize);
                if(inputStream != null){
                    mFTPClient.enterLocalPassiveMode();
                    mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                    mFTPClient.setBufferSize(1024 * 1024 * 5); //5M
                    outputStream = mFTPClient.storeFileStream(remotePathName);
//                boolean ret = mFTPClient.storeFile(remotePathName, inputStream);
                    if(outputStream != null){
                        byte[] buf = new byte[44 * 1460];
                        while ((length = inputStream.read(buf)) != -1){
                            outputStream.write(buf, 0, length);
                            size += length;
                            if(localFileSize > 0){
                                progress = (int)(size * 100 / localFileSize);
                                if( progress % 2 == 0){
                                    Dbug.e(tag, "-uploadFile- progress : " +progress);
                                    if(handler != null){
                                        handler.sendMessage(handler.obtainMessage(MSG_UPDATE_UPLOAD_PROGRESS, progress, 0));
                                    }
                                }
                            }
                        }
                        SystemClock.sleep(2000);
                        disconnect();
                        Dbug.e(tag, "-uploadFile- size : " +size+" ,localFileSize = " +localFileSize);
                        if(size >= localFileSize){
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        disconnect();
        return false;
    }
}
