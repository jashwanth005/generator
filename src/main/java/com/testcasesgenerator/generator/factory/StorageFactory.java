package com.testcasesgenerator.generator.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.testcasesgenerator.generator.Services.MySqlStorageService;
import com.testcasesgenerator.generator.Services.S3StorageService;
import com.testcasesgenerator.generator.Services.StorageService;

/**
 * Factory to provide the configured storage service
 */
@Component
public class StorageFactory {

    private final MySqlStorageService mySqlStorageService;
    private final S3StorageService s3StorageService;
    
    @Value("${storage.type:mysql}")
    private String storageType;

    @Autowired
    public StorageFactory(
            MySqlStorageService mySqlStorageService,
            S3StorageService s3StorageService) {
        this.mySqlStorageService = mySqlStorageService;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Get the configured storage service
     * 
     * @return The storage service implementation
     */
    public StorageService getStorageService() {
        switch (storageType.toLowerCase()) {
            case "s3":
                return s3StorageService;
            case "mysql":
            default:
                return mySqlStorageService;
        }
    }
}