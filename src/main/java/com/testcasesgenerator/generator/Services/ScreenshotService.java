package com.testcasesgenerator.generator.Services;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ScreenshotService {

    public String captureScreenshot(WebDriver driver, String executionDir, String stepName) {
        try {
            if (driver == null) {
                System.err.println("Driver is null, cannot capture screenshot");
                return null;
            }
            
            // Check if driver is still connected
            try {
                driver.getCurrentUrl(); // Test if driver is responsive
            } catch (Exception e) {
                System.err.println("Driver is not responsive, cannot capture screenshot: " + e.getMessage());
                return null;
            }
            
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = stepName + "_" + timestamp + ".png";
            String screenshotPath = executionDir + "/" + fileName;
            
            Path destPath = Paths.get(screenshotPath);
            Files.createDirectories(destPath.getParent());
            Files.copy(sourceFile.toPath(), destPath);
            
            System.out.println("Screenshot captured: " + screenshotPath);
            return screenshotPath;
            
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot for " + stepName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 