# AI Test Automation Agent

## Overview

This AI Test Automation Agent extends your existing test case generator to create a complete end-to-end automation pipeline. The agent can:

1. **Generate Test Cases** from Jira tickets using AI (OpenAI/Toqua)
2. **Create Automation Scripts** from test cases using AI
3. **Execute Scripts** with Selenium WebDriver
4. **Capture Screenshots** at every step
5. **Generate Reports** with comprehensive HTML reports

## Architecture

```
Jira Ticket → AI Test Cases → AI Automation Scripts → Selenium Execution → Screenshot Capture → HTML Report
```

## New Components

### Models
- `AutomationScript` - Stores generated automation scripts
- `TestExecutionResult` - Stores test execution results and screenshots
- `TestStep` - Represents individual test steps

### Services
- `AutomationScriptGeneratorService` - AI-powered script generation
- `TestExecutionService` - Selenium script execution with screenshot capture
- `TestReportGeneratorService` - HTML report generation
- `ScreenshotService` - Screenshot management

### Controllers
- `AIAgentController` - Complete AI agent workflow endpoints

## API Endpoints

### 1. Complete AI Agent Workflow
```http
POST /api/ai-agent/run-complete-ai-agent-workflow
Content-Type: application/json

{
    "ticketId": "PROJ-123",
    "baseUrl": "https://your-app.com",
    "scriptLanguage": "JAVA_SELENIUM"
}
```

**Response:**
```json
{
    "success": true,
    "message": "AI Agent workflow completed successfully",
    "ticketId": "PROJ-123",
    "testCasesGenerated": true,
    "scriptsGenerated": 5,
    "testsExecuted": 5,
    "passedTests": 4,
    "failedTests": 1,
    "errorTests": 0,
    "passRate": 80.0,
    "reportPath": "test-reports/html/PROJ-123/20231201_143022/test_report.html",
    "reportUrl": "/api/ai-agent/view-report?reportPath=..."
}
```

### 2. Generate Automation Scripts Only
```http
POST /api/ai-agent/generate-automation-scripts
Content-Type: application/json

{
    "ticketId": "PROJ-123",
    "testCasesContent": "- Test Case ID: TC001\n- Scenario: Login test\n...",
    "baseUrl": "https://your-app.com",
    "scriptLanguage": "JAVA_SELENIUM"
}
```

### 3. Execute Automation Scripts
```http
POST /api/ai-agent/execute-automation-scripts?ticketId=PROJ-123
```

### 4. Generate Test Report
```http
GET /api/ai-agent/generate-test-report?ticketId=PROJ-123
```

### 5. Check Execution Status
```http
GET /api/ai-agent/execution-status?ticketId=PROJ-123
```

### 6. View Generated Report
```http
GET /api/ai-agent/view-report?reportPath=test-reports/html/PROJ-123/20231201_143022/test_report.html
```

## Supported Script Languages

- `JAVA_SELENIUM` - Java with Selenium WebDriver and TestNG
- `PYTHON_SELENIUM` - Python with Selenium WebDriver and pytest
- More languages can be easily added

## Configuration

### Environment Variables (.env)
```properties
# Existing variables
OPENAI_API_KEY=your_openai_key
OPENAI_API_URL=https://api.openai.com/v1/chat/completions
TOQAN_API_KEY=your_toqua_key
TOQAN_API_URL=your_toqua_url
ticketId=PROJ-123

# New variables for AI Agent
AI_AGENT_SCREENSHOT_QUALITY=HIGH
AI_AGENT_BROWSER_HEADLESS=true
AI_AGENT_EXECUTION_TIMEOUT=300000
```

### Application Properties
```properties
# Existing configuration remains the same
# AI Agent creates directories automatically:
# - test-reports/screenshots/
# - test-reports/html/
# - test-reports/scripts/
```

## Usage Examples

### 1. Complete Workflow (Recommended)
```bash
curl -X POST http://localhost:8083/api/ai-agent/run-complete-ai-agent-workflow \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": "PROJ-123",
    "baseUrl": "https://example.com",
    "scriptLanguage": "JAVA_SELENIUM"
  }'
```

### 2. Step-by-Step Workflow
```bash
# Step 1: Generate test cases (existing endpoint)
curl "http://localhost:8083/getTestCases?ticketId=PROJ-123"

# Step 2: Generate automation scripts
curl -X POST http://localhost:8083/api/ai-agent/generate-automation-scripts \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": "PROJ-123",
    "testCasesContent": "...",
    "baseUrl": "https://example.com",
    "scriptLanguage": "JAVA_SELENIUM"
  }'

# Step 3: Execute scripts
curl -X POST "http://localhost:8083/api/ai-agent/execute-automation-scripts?ticketId=PROJ-123"

# Step 4: Generate report
curl "http://localhost:8083/api/ai-agent/generate-test-report?ticketId=PROJ-123"
```

## Generated Artifacts

### 1. Automation Scripts
- **Location**: Database (automation_scripts table)
- **Content**: Complete executable automation scripts
- **Languages**: Java/Selenium, Python/Selenium, etc.

### 2. Screenshots
- **Location**: `test-reports/screenshots/{ticketId}/{testCaseId}/{timestamp}/`
- **Format**: PNG files with timestamps
- **Captured**: Every step, on errors, and at completion

### 3. HTML Reports
- **Location**: `test-reports/html/{ticketId}/{timestamp}/test_report.html`
- **Features**: 
  - Interactive expandable test cases
  - Screenshot gallery
  - Execution statistics
  - Error details with stack traces
  - Beautiful responsive design

### 4. Database Records
- **automation_scripts**: Generated scripts with metadata
- **test_execution_results**: Execution results with timing and status
- **test_cases**: Original test cases (existing)

## Report Features

The generated HTML reports include:

1. **Executive Summary Dashboard**
   - Total tests executed
   - Pass/Fail/Error counts
   - Pass rate percentage
   - Execution timing

2. **Interactive Test Details**
   - Expandable test case sections
   - Step-by-step execution results
   - Screenshots for each step
   - Error details and stack traces

3. **Modern UI**
   - Responsive design
   - Color-coded status indicators
   - Professional styling
   - Easy navigation

## Error Handling

The AI Agent includes comprehensive error handling:

- **Script Generation Failures**: Fallback from Toqua AI to OpenAI
- **Execution Failures**: Captured with screenshots and error details
- **Screenshot Failures**: Graceful degradation without stopping execution
- **Report Generation**: Error pages with diagnostic information

## Performance Considerations

- **Headless Browser**: Executions run in headless mode for performance
- **Parallel Execution**: Future enhancement for running multiple tests in parallel
- **Cleanup**: Old screenshots and reports can be cleaned up automatically
- **Caching**: Leverages existing Redis cache for test cases

## Future Enhancements

1. **Parallel Test Execution**: Run multiple scripts simultaneously
2. **Cross-Browser Testing**: Support for Firefox, Safari, Edge
3. **Mobile Testing**: Appium integration for mobile automation
4. **API Testing**: REST API automation scripts
5. **CI/CD Integration**: Jenkins/GitHub Actions plugins
6. **Real Device Testing**: Integration with cloud testing platforms
7. **Visual Testing**: Screenshot comparison and visual regression
8. **Performance Testing**: Load testing capabilities

## Troubleshooting

### Common Issues

1. **Chrome Driver Issues**
   - Solution: WebDriverManager automatically handles driver downloads

2. **Screenshot Failures**
   - Check write permissions on test-reports directory
   - Ensure sufficient disk space

3. **Script Generation Failures**
   - Verify AI API keys in .env file
   - Check network connectivity to AI services

4. **Execution Timeouts**
   - Adjust AI_AGENT_EXECUTION_TIMEOUT environment variable
   - Check target application response times

### Logs Location
- Application logs: Console output
- Screenshot paths: Logged in application output
- Execution results: Stored in database with timestamps

## Integration with Existing System

The AI Agent seamlessly integrates with your existing system:

- **Same Database**: Uses existing MySQL database with new tables
- **Same Cache**: Leverages existing Redis cache
- **Same AI Services**: Uses existing OpenAI and Toqua AI integrations
- **Same APIs**: Extends existing REST API structure
- **Same Configuration**: Uses existing Spring Boot configuration

This ensures minimal disruption while adding powerful automation capabilities to your test case generation workflow. 