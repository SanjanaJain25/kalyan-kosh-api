package com.example.kalyan_kosh_api.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file);
    String store(MultipartFile file, String subdirectory);
    String storeWithCustomName(MultipartFile file, String subdirectory, String customName);
}
