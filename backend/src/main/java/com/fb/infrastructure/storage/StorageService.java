package com.fb.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;

public interface StorageService {

    default void initialize() {}

    String uploadFile(MultipartFile file, String directory);

    String uploadFile(InputStream inputStream, String fileName, String contentType, String directory);

    byte[] downloadFile(String fileUrl);

    InputStream downloadFileAsInputStream(String fileUrl);

    void deleteFile(String fileUrl);

    boolean fileExists(String fileUrl);

    String getPresignedUrl(String fileUrl, long expirationMinutes);

    URL getFileUrl(String fileUrl);

    String generateFileName(String originalFileName);
}
