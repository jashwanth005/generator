package com.testcasesgenerator.generator.Services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Service
public class ScreenshotService {

    private static final String SCREENSHOTS_BASE_DIR = "test-reports/screenshots";
    
    public String captureScreenshot(WebDriver driver, String testCaseId, String stepName) {
        return captureScreenshot(driver, testCaseId, stepName, null);
    }
    
    public String captureScreenshot(WebDriver driver, String testCaseId, String stepName, String customDir) {
        try {
            if (!(driver instanceof TakesScreenshot)) {
                throw new RuntimeException("Driver does not support screenshot capture");
            }
            
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = stepName + "_" + timestamp + ".png";
            
            String screenshotDir = customDir != null ? customDir : 
                SCREENSHOTS_BASE_DIR + "/" + testCaseId + "/" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // Create directory if it doesn't exist
            File directory = new File(screenshotDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String screenshotPath = screenshotDir + "/" + fileName;
            File destFile = new File(screenshotPath);
            
            FileUtils.copyFile(sourceFile, destFile);
            
            System.out.println("Screenshot captured: " + screenshotPath);
            return screenshotPath;
            
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot for step " + stepName + ": " + e.getMessage());
            return null;
        }
    }
    
    public String captureFullPageScreenshot(WebDriver driver, String testCaseId, String stepName) {
        // For full page screenshots, we might need to use different approach
        // For now, using the same method
        return captureScreenshot(driver, testCaseId, stepName + "_fullpage");
    }
    
    public boolean deleteScreenshot(String screenshotPath) {
        try {
            File file = new File(screenshotPath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to delete screenshot: " + e.getMessage());
            return false;
        }
    }
    
    public void cleanupOldScreenshots(int daysOld) {
        // Implementation to clean up old screenshots
        File screenshotsDir = new File(SCREENSHOTS_BASE_DIR);
        if (screenshotsDir.exists()) {
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000);
            cleanupDirectory(screenshotsDir, cutoffTime);
        }
    }
    
    private void cleanupDirectory(File directory, long cutoffTime) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanupDirectory(file, cutoffTime);
                } else if (file.lastModified() < cutoffTime) {
                    file.delete();
                }
            }
        }
    }
} 