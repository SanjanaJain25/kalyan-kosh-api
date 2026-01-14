package com.example.kalyan_kosh_api.service.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
            File dir = new File(BASE_UPLOAD_DIR + "/" + subdirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Build filename: customName_timestamp.ext or uuid_originalname
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename;

            if (customName != null && !customName.isEmpty()) {
                filename = customName + "_" + System.currentTimeMillis() + extension;
            } else {
                filename = System.currentTimeMillis() + "_" + originalFilename;
            }

            File dest = new File(dir, filename);
            file.transferTo(dest);

            return dest.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
