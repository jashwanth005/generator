package com.testcasesgenerator.generator.Services;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class ExcelService {

    public void saveTestCasesToExcel(String testCaseString, String filePath) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        // Split the test case string into individual test cases
        String[] testCases = testCaseString.split("- Test Case ID: ");
        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Test Case ID");
        headerRow.createCell(1).setCellValue("Scenario");
        headerRow.createCell(2).setCellValue("Steps");
        headerRow.createCell(3).setCellValue("Expected Result");

        // Process each test case
        for (String testCase : testCases) {
            if (testCase.trim().isEmpty()) continue;
            Row row = sheet.createRow(rowNum++);
            String[] lines = testCase.split("\n");
            String caseId = lines[0].trim();
            String scenario = "";
            StringBuilder steps = new StringBuilder();
            String expectedResult = "";
            boolean isStepsSection = false;
            boolean isExpectedResultSection = false;

            for (String line : lines) {
                line = line.trim(); // Ensure no leading/trailing spaces

                if (line.contains("Scenario:")) {
                    scenario = line.split(":")[1].trim();
                } else if (line.contains("Expected Result:")) {
                    expectedResult = line.split(":")[1].trim();
                    isExpectedResultSection = true;
                } else if (line.matches("^\\d+\\..*")) {
                    isStepsSection = true;
                    steps.append(line).append("\n");
                    isExpectedResultSection = false; // Steps section means we're out of the expected result section
                } else if (isStepsSection && !isExpectedResultSection) {
                    steps.append(line).append("\n");  // Add continuation of steps if any
                } else if (isExpectedResultSection) {
                    // Continue adding lines to expectedResult if multi-line expected result
                    expectedResult += " " + line;
                }
            }

            // Add data to Excel row
            row.createCell(0).setCellValue(caseId);
            row.createCell(1).setCellValue(scenario);
            row.createCell(2).setCellValue(steps.toString().trim());
            row.createCell(3).setCellValue(expectedResult.trim());  // Ensure any extra spaces are trimmed
        }

        // Save Excel file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        System.out.println("Test cases saved to " + filePath);
    }
}
