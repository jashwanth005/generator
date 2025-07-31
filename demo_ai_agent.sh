#!/bin/bash

# AI Test Automation Agent Demo Script
# This script demonstrates the complete AI agent workflow

set -e

# Configuration
BASE_URL="http://localhost:8083"
TICKET_ID="DEMO-123"
APP_BASE_URL="https://example.com"
SCRIPT_LANGUAGE="JAVA_SELENIUM"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to make API calls with better formatting
make_api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    print_step "$description"
    echo "Making $method request to: $BASE_URL$endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        print_success "API call successful (HTTP $http_code)"
        echo "Response: $body" | jq '.' 2>/dev/null || echo "Response: $body"
    else
        print_error "API call failed (HTTP $http_code)"
        echo "Response: $body"
        return 1
    fi
    
    echo ""
    return 0
}

# Function to check if the application is running
check_application() {
    print_step "Checking if application is running..."
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_success "Application is running on $BASE_URL"
    else
        print_error "Application is not running on $BASE_URL"
        echo "Please start the application first: mvn spring-boot:run"
        exit 1
    fi
    echo ""
}

# Function to wait for user input
wait_for_user() {
    echo -e "${YELLOW}Press Enter to continue or Ctrl+C to exit...${NC}"
    read -r
}

# Main demo
main() {
    echo "=========================================="
    echo "       AI Test Automation Agent Demo"
    echo "=========================================="
    echo ""
    echo "This demo will show you how to:"
    echo "1. Generate test cases from a Jira ticket"
    echo "2. Create automation scripts using AI"
    echo "3. Execute the scripts with screenshot capture"
    echo "4. Generate a comprehensive HTML report"
    echo ""
    
    # Check if application is running
    check_application
    
    echo "Demo Configuration:"
    echo "- Base URL: $BASE_URL"
    echo "- Ticket ID: $TICKET_ID"
    echo "- App Base URL: $APP_BASE_URL"
    echo "- Script Language: $SCRIPT_LANGUAGE"
    echo ""
    
    wait_for_user
    
    # Method 1: Complete Workflow (Recommended)
    echo "=========================================="
    echo "      Method 1: Complete AI Workflow"
    echo "=========================================="
    echo ""
    
    complete_workflow_data='{
        "ticketId": "'$TICKET_ID'",
        "baseUrl": "'$APP_BASE_URL'",
        "scriptLanguage": "'$SCRIPT_LANGUAGE'"
    }'
    
    if make_api_call "POST" "/api/ai-agent/run-complete-ai-agent-workflow" "$complete_workflow_data" "Running complete AI agent workflow"; then
        print_success "Complete workflow finished! Check the response for report URL."
        wait_for_user
    else
        print_warning "Complete workflow failed. Let's try step-by-step approach..."
        wait_for_user
    fi
    
    # Method 2: Step-by-Step Workflow
    echo "=========================================="
    echo "      Method 2: Step-by-Step Workflow"
    echo "=========================================="
    echo ""
    
    # Step 1: Generate test cases (existing endpoint)
    if make_api_call "GET" "/getTestCases?ticketId=$TICKET_ID" "" "Step 1: Generating test cases from Jira ticket"; then
        wait_for_user
    fi
    
    # Step 2: Generate automation scripts
    script_generation_data='{
        "ticketId": "'$TICKET_ID'",
        "testCasesContent": "- Test Case ID: TC001\n- Scenario: Verify homepage loading\n- Steps:\n    1. Navigate to the application URL\n    2. Wait for page to load\n    3. Verify page title is not empty\n- Expected Result: Page should load successfully with a valid title\n\n- Test Case ID: TC002\n- Scenario: Verify navigation menu\n- Steps:\n    1. Navigate to the application URL\n    2. Locate the navigation menu\n    3. Verify menu items are visible\n- Expected Result: Navigation menu should be visible and functional",
        "baseUrl": "'$APP_BASE_URL'",
        "scriptLanguage": "'$SCRIPT_LANGUAGE'"
    }'
    
    if make_api_call "POST" "/api/ai-agent/generate-automation-scripts" "$script_generation_data" "Step 2: Generating automation scripts using AI"; then
        wait_for_user
    fi
    
    # Step 3: Execute automation scripts
    if make_api_call "POST" "/api/ai-agent/execute-automation-scripts?ticketId=$TICKET_ID" "" "Step 3: Executing automation scripts with screenshot capture"; then
        wait_for_user
    fi
    
    # Step 4: Generate test report
    if make_api_call "GET" "/api/ai-agent/generate-test-report?ticketId=$TICKET_ID" "" "Step 4: Generating comprehensive HTML test report"; then
        wait_for_user
    fi
    
    # Step 5: Check execution status
    if make_api_call "GET" "/api/ai-agent/execution-status?ticketId=$TICKET_ID" "" "Step 5: Checking final execution status"; then
        echo ""
    fi
    
    # Final information
    echo "=========================================="
    echo "              Demo Complete!"
    echo "=========================================="
    echo ""
    echo "What was created:"
    echo "✓ Test cases from Jira ticket $TICKET_ID"
    echo "✓ AI-generated automation scripts (Java/Selenium)"
    echo "✓ Test execution with screenshot capture"
    echo "✓ Comprehensive HTML report with:"
    echo "  - Execution statistics"
    echo "  - Step-by-step results"
    echo "  - Screenshots for each step"
    echo "  - Error details (if any)"
    echo ""
    echo "Files created:"
    echo "- Database records in automation_scripts table"
    echo "- Database records in test_execution_results table"
    echo "- Screenshots in: test-reports/screenshots/$TICKET_ID/"
    echo "- HTML report in: test-reports/html/$TICKET_ID/"
    echo ""
    echo "Next steps:"
    echo "1. Check the generated HTML report (URL provided in responses)"
    echo "2. Review screenshots in the test-reports directory"
    echo "3. Examine database records for detailed information"
    echo "4. Try with your own Jira tickets and applications"
    echo ""
    
    print_success "AI Test Automation Agent demo completed successfully!"
}

# Check if jq is installed for JSON formatting
if ! command -v jq &> /dev/null; then
    print_warning "jq is not installed. JSON responses will not be formatted."
    echo "Install jq for better output: sudo apt-get install jq (Ubuntu) or brew install jq (Mac)"
    echo ""
fi

# Run the demo
main "$@" 