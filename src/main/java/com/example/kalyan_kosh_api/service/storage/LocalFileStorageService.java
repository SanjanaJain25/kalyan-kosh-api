package com.example.kalyan_kosh_api.service.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

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
        try {
            // Use absolute path from current working directory
            Path baseDir = Paths.get(System.getProperty("user.dir"), BASE_UPLOAD_DIR, subdirectory);

            // Create directories if they don't exist
            Files.createDirectories(baseDir);

            // Build filename: customName_timestamp.ext or uuid_originalname
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename;

            if (customName != null && !customName.isEmpty()) {
                filename = customName + "_" + System.currentTimeMillis() + extension;
            } else {
                filename = System.currentTimeMillis() + "_" + originalFilename;
            }

            Path destPath = baseDir.resolve(filename);

            // Copy file using InputStream (more reliable than transferTo)
            Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            return destPath.toAbsolutePath().toString();
        } catch (IOException e) {
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
