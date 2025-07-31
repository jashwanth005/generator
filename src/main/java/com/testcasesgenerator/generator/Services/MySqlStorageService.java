package com.testcasesgenerator.generator.Services;

import com.testcasesgenerator.generator.Model.TestCaseFile;
import com.testcasesgenerator.generator.Repository.TestCaseFileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class MySqlStorageService implements StorageService {

    private final TestCaseFileRepository testCaseFileRepository;
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @Value("${server.address:localhost}")
    private String serverAddress;

    @Autowired
    public MySqlStorageService(TestCaseFileRepository testCaseFileRepository) {
        this.testCaseFileRepository = testCaseFileRepository;
    }

    @Override
    public String storeTestCase(String ticketId, String filePath, String title, String description) {
        TestCaseFile testCaseFile = new TestCaseFile(ticketId, filePath, title, description);
        testCaseFileRepository.save(testCaseFile);
        return generateDownloadUrl(ticketId);
    }

    @Override
    public Optional<String> getDownloadUrl(String ticketId) {
        return testCaseFileRepository.findByTicketId(ticketId)
                .map(testCaseFile -> generateDownloadUrl(ticketId));
    }

    @Override
    public boolean exists(String ticketId) {
        return testCaseFileRepository.findByTicketId(ticketId).isPresent();
    }

    @Override
    public boolean delete(String ticketId) {
        Optional<TestCaseFile> testCaseFileOpt = testCaseFileRepository.findByTicketId(ticketId);
        if (testCaseFileOpt.isPresent()) {
            TestCaseFile testCaseFile = testCaseFileOpt.get();
            
            try {
                File file = new File(testCaseFile.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                System.err.println("Error deleting file: " + e.getMessage());
            }
            
            testCaseFileRepository.delete(testCaseFile);
            return true;
        }
        return false;
    }
    
    private String generateDownloadUrl(String ticketId) {
        return "http://" + serverAddress + ":" + serverPort + "/download?ticketId=" + ticketId;
    }
}