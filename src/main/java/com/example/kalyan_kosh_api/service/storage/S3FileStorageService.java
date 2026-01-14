package com.example.kalyan_kosh_api.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

//    @Value("${aws.s3.bucket-name}")
    private String bucketName="pmum-qr-death-case";

//    @Value("${aws.s3.region}")
    private String region ="ap-south-1";

//    @Value("${aws.s3.access-key:}")
    private String accessKey="AKIAWWDH2AQTZOLZH3WI";

//    @Value("${aws.s3.secret-key:}")
    private String secretKey= "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";

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)  // Use path-style access to avoid region redirect issues
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(s3Config)
                .endpointOverride(URI.create("https://s3." + region + ".amazonaws.com"));

        // If access key and secret key are provided, use them
        // Otherwise, use default credentials provider (IAM Role on EC2)
        if (accessKey != null && !accessKey.isEmpty() &&
            !accessKey.equals("your-access-key") &&
            secretKey != null && !secretKey.isEmpty() &&
            !secretKey.equals("your-secret-key")) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        this.s3Client = builder.build();
    }

    @Override
    public String store(MultipartFile file) {
        return store(file, "receipts");
    }

    @Override
    public String store(MultipartFile file, String subdirectory) {
        return storeWithCustomName(file, subdirectory, null);
    }

    @Override
    public String storeWithCustomName(MultipartFile file, String subdirectory, String customName) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename;

            if (customName != null && !customName.isEmpty()) {
                filename = customName + "_" + System.currentTimeMillis() + extension;
            } else {
                filename = System.currentTimeMillis() + "_" + originalFilename;
            }

            String key = subdirectory + "/" + filename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Return the S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in S3", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}

