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
        String[] testCases = testCaseString.split("- Test Case ID: ");
        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Test Case ID");
        headerRow.createCell(1).setCellValue("Scenario");
        headerRow.createCell(2).setCellValue("Steps");
        headerRow.createCell(3).setCellValue("Expected Result");
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
                line = line.trim(); /

                if (line.contains("Scenario:")) {
                    scenario = line.split(":")[1].trim();
                } else if (line.contains("Expected Result:")) {
                    expectedResult = line.split(":")[1].trim();
                    isExpectedResultSection = true;
                } else if (line.matches("^\\d+\\..*")) {
                    isStepsSection = true;
                    steps.append(line).append("\n");
                    isExpectedResultSection = false; 
                } else if (isStepsSection && !isExpectedResultSection) {
                    steps.append(line).append("\n");  
                } else if (isExpectedResultSection) {
                    expectedResult += " " + line;
                }
            }
            row.createCell(0).setCellValue(caseId);
            row.createCell(1).setCellValue(scenario);
            row.createCell(2).setCellValue(steps.toString().trim());
            row.createCell(3).setCellValue(expectedResult.trim());
        }

        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        System.out.println("Test cases saved to " + filePath);
    }
}
