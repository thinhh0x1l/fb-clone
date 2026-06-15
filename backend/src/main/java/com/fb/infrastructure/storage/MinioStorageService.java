package com.fb.infrastructure.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:facebook-storage}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public void initialize() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": ["*"]},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
                log.info("Bucket '{}' created with public read policy", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = directory + "/" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, String directory) {
        try {
            String objectName = directory + "/" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public byte[] downloadFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    @Override
    public InputStream downloadFileAsInputStream(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String fileUrl, long expirationMinutes) {
        try {
            String objectName = extractObjectName(fileUrl);
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry((int) expirationMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public URL getFileUrl(String fileUrl) {
        try {
            return java.net.URI.create(fileUrl).toURL();
        } catch (Exception e) {
            throw new RuntimeException("Invalid file URL", e);
        }
    }

    @Override
    public String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String extractObjectName(String fileUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        return fileUrl;
    }
}
