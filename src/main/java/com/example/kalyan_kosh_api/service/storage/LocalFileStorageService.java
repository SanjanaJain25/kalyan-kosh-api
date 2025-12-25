package com.example.kalyan_kosh_api.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/receipts";

    @Override
    public String store(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(dir, filename);
            file.transferTo(dest);

            return dest.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
