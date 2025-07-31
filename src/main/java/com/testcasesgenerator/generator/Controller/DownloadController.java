package com.testcasesgenerator.generator.Controller;

import com.testcasesgenerator.generator.Model.TestCaseFile;
import com.testcasesgenerator.generator.Repository.TestCaseFileRepository;
import com.testcasesgenerator.generator.factory.StorageFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class DownloadController {

    private final TestCaseFileRepository testCaseFileRepository;
    private final StorageFactory storageFactory;

    @Autowired
    public DownloadController(TestCaseFileRepository testCaseFileRepository, StorageFactory storageFactory) {
        this.testCaseFileRepository = testCaseFileRepository;
        this.storageFactory = storageFactory;
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String ticketId) {
        Optional<TestCaseFile> testCaseFileOpt = testCaseFileRepository.findByTicketId(ticketId);
        
        if (testCaseFileOpt.isPresent()) {
            TestCaseFile testCaseFile = testCaseFileOpt.get();
            String filePath = testCaseFile.getFilePath();
            
            // Check if it's an S3 URL
            if (filePath.startsWith("http") || filePath.startsWith("https")) {
                // For S3 URLs, redirect to the S3 URL
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, filePath)
                        .build();
            } else {
                // For local files, serve the file
                try {
                    Path path = Paths.get(filePath);
                    Resource resource = new UrlResource(path.toUri());
                    
                    if (resource.exists()) {
                        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(contentType))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                                .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (MalformedURLException e) {
                    return ResponseEntity.badRequest().build();
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}