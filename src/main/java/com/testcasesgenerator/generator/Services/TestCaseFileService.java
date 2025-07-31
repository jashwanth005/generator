package com.testcasesgenerator.generator.Services;

import com.testcasesgenerator.generator.Model.TestCaseFile;
import com.testcasesgenerator.generator.Repository.TestCaseFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TestCaseFileService {

    private final TestCaseFileRepository testCaseFileRepository;
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @Value("${server.address:localhost}")
    private String serverAddress;

    @Autowired
    public TestCaseFileService(TestCaseFileRepository testCaseFileRepository) {
        this.testCaseFileRepository = testCaseFileRepository;
    }

    public TestCaseFile saveTestCaseFile(String ticketId, String filePath, String title, String description) {
        TestCaseFile testCaseFile = new TestCaseFile(ticketId, filePath, title, description);
        return testCaseFileRepository.save(testCaseFile);
    }

    public Optional<TestCaseFile> findByTicketId(String ticketId) {
        return testCaseFileRepository.findByTicketId(ticketId);
    }
    
    public String generateDownloadUrl(String ticketId) {
        return "http://" + serverAddress + ":" + serverPort + "/download?ticketId=" + ticketId;
    }
}
