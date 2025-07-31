package com.testcasesgenerator.generator.Services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.testcasesgenerator.generator.Model.TestCaseFile;
import com.testcasesgenerator.generator.Repository.TestCaseFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class S3StorageService implements StorageService {

    private final AmazonS3 amazonS3;
    private final TestCaseFileRepository testCaseFileRepository;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public S3StorageService(AmazonS3 amazonS3, TestCaseFileRepository testCaseFileRepository) {
        this.amazonS3 = amazonS3;
        this.testCaseFileRepository = testCaseFileRepository;
    }

    @Override
    public String storeTestCase(String ticketId, String filePath, String title, String description) {
        // Upload file to S3
        String s3Key = "test-cases/" + ticketId + ".xlsx";
        File file = new File(filePath);
        
        // Upload to S3 with public read access
        amazonS3.putObject(
            new PutObjectRequest(bucketName, s3Key, file)
                .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        
        // Get S3 URL
        String s3Url = amazonS3.getUrl(bucketName, s3Key).toString();
        
        // Save metadata to database
        TestCaseFile testCaseFile = new TestCaseFile(ticketId, s3Url, title, description);
        testCaseFileRepository.save(testCaseFile);
        
        return s3Url;
    }

    @Override
    public Optional<String> getDownloadUrl(String ticketId) {
        return testCaseFileRepository.findByTicketId(ticketId)
                .map(TestCaseFile::getFilePath);
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
            
            // Extract S3 key from the URL
            String s3Url = testCaseFile.getFilePath();
            String s3Key = s3Url.substring(s3Url.indexOf(bucketName) + bucketName.length() + 1);
            
            // Delete from S3
            try {
                amazonS3.deleteObject(bucketName, s3Key);
            } catch (Exception e) {
                System.err.println("Error deleting file from S3: " + e.getMessage());
            }
            
            // Delete database record
            testCaseFileRepository.delete(testCaseFile);
            return true;
        }
        return false;
    }
}