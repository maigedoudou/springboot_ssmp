package com.donna.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class FtpService {

    @Value("${ftp.server.host:ftp-server}")
    private String server;

    @Value("${ftp.server.port:21}")
    private int port;

    @Value("${ftp.server.username:anonymous}")
    private String user;

    @Value("${ftp.server.password:anonymous}")
    private String pass;

    public boolean uploadFile(File localFile, String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            // 尝试连接到 FTP 服务器
            ftpClient.connect(server, port);
            if (!ftpClient.login(user, pass)) {
                throw new IOException("FTP login failed");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            try (FileInputStream fis = new FileInputStream(localFile)) {
                return ftpClient.storeFile(remoteFileName, fis);
            }
        } catch (IOException e) {
            System.err.println("Error during FTP operation: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪
            throw e; // 重新抛出异常
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                System.err.println("Error disconnecting from FTP server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void downloadFile(String remoteFileName, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            // 尝试连接到 FTP 服务器
            ftpClient.connect(server, port);
            if (!ftpClient.login(user, pass)) {
                throw new IOException("FTP login failed");
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(new File(localFilePath))) {
                if (!ftpClient.retrieveFile(remoteFileName, outputStream)) {
                    throw new IOException("Failed to download file: " + remoteFileName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error during FTP operation: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪
            throw e; // 重新抛出异常
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                System.err.println("Error disconnecting from FTP server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 