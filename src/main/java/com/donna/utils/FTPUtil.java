package com.donna.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    @Value("${ftp.server.host:ftp-server}")
    private String host;

    @Value("${ftp.server.port:21}")
    private int port;

    @Value("${ftp.server.username:anonymous}")
    private String username;

    @Value("${ftp.server.password:anonymous}")
    private String password;

    /**
     * 连接FTP服务器
     */
    private FTPClient connectFTPServer() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setConnectTimeout(5000); // 设置连接超时
            ftpClient.connect(host, port);
            if (!ftpClient.login(username, password)) {
                logger.error("FTP login failed.");
                return null;
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");
            
            // 检查连接是否成功
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.error("FTP server refused connection.");
                return null;
            }
            return ftpClient;
        } catch (IOException e) {
            logger.error("Could not connect to FTP server.", e);
            return null;
        }
    }

    /**
     * 上传文件到FTP服务器
     */
    public boolean uploadFile(String remoteFileName, InputStream inputStream) {
        FTPClient ftpClient = connectFTPServer();
        if (ftpClient == null) return false;

        try {
            boolean result = ftpClient.storeFile(remoteFileName, inputStream);
            inputStream.close();
            return result;
        } catch (IOException e) {
            logger.error("Could not upload file to FTP server.", e);
            return false;
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.error("Error disconnecting from FTP server.", e);
            }
        }
    }

    /**
     * 从FTP服务器下载文件
     */
    public boolean downloadFile(String remoteFileName, String localFileName) {
        FTPClient ftpClient = connectFTPServer();
        if (ftpClient == null) return false;

        try (FileOutputStream fos = new FileOutputStream(localFileName)) {
            boolean result = ftpClient.retrieveFile(remoteFileName, fos);
            return result;
        } catch (IOException e) {
            logger.error("Could not download file from FTP server.", e);
            return false;
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.error("Error disconnecting from FTP server.", e);
            }
        }
    }

    /**
     * 导出图书数据到CSV并上传到FTP
     */
    public boolean exportBooksToFTP(String content, String fileName) {
        try {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
            return uploadFile("/books/" + fileName, inputStream);
        } catch (IOException e) {
            logger.error("Could not export books to FTP.", e);
            return false;
        }
    }
}