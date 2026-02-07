package com.example.kalyan_kosh_api.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final String BASE_UPLOAD_DIR = "uploads";

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
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to store null or empty file");
            return null;
        }

        try {
            // Use absolute path from current working directory
            String userDir = System.getProperty("user.dir");
            Path baseDir = Paths.get(userDir, BASE_UPLOAD_DIR, subdirectory);

            log.info("Storing file - UserDir: {}, BaseDir: {}, Subdirectory: {}",
                     userDir, baseDir.toAbsolutePath(), subdirectory);

            // Create directories if they don't exist
            Files.createDirectories(baseDir);
            log.info("Created/verified directory: {}", baseDir.toAbsolutePath());

            // Build filename: customName_timestamp.ext or uuid_originalname
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename;

            if (customName != null && !customName.isEmpty()) {
                // Sanitize custom name to remove invalid characters
                String sanitizedCustomName = customName.replaceAll("[^a-zA-Z0-9_-]", "_");
                filename = sanitizedCustomName + "_" + System.currentTimeMillis() + extension;
            } else {
                // Sanitize original filename
                String sanitizedOriginal = originalFilename != null ?
                    originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
                filename = System.currentTimeMillis() + "_" + sanitizedOriginal;
            }

            Path destPath = baseDir.resolve(filename);
            log.info("Destination path: {}, File size: {} bytes, Content type: {}",
                     destPath.toAbsolutePath(), file.getSize(), file.getContentType());

            // Copy file using InputStream (more reliable than transferTo)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Successfully stored file: {}", destPath.toAbsolutePath());
            return destPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to store file. Subdirectory: {}, CustomName: {}, Error: {}",
                      subdirectory, customName, e.getMessage(), e);
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
