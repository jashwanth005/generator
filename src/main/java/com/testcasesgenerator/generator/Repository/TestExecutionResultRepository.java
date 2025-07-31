package com.testcasesgenerator.generator.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.testcasesgenerator.generator.Model.TestExecutionResult;

@Repository
public interface TestExecutionResultRepository extends JpaRepository<TestExecutionResult, Long> {
    
    List<TestExecutionResult> findByTicketId(String ticketId);
    
    List<TestExecutionResult> findByTicketIdAndExecutionStatus(String ticketId, String executionStatus);
    
    Optional<TestExecutionResult> findByTestCaseId(String testCaseId);
    
    List<TestExecutionResult> findByAutomationScriptId(Long automationScriptId);
    
    List<TestExecutionResult> findByExecutionStatus(String executionStatus);
    
    @Query("SELECT t FROM TestExecutionResult t WHERE t.executionStartTime BETWEEN :startDate AND :endDate")
    List<TestExecutionResult> findByExecutionDateRange(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM TestExecutionResult t WHERE t.ticketId = :ticketId AND t.executionStatus = :status")
    long countByTicketIdAndStatus(@Param("ticketId") String ticketId, @Param("status") String status);
    
    @Query("SELECT t FROM TestExecutionResult t WHERE t.ticketId = :ticketId ORDER BY t.executionStartTime DESC")
    List<TestExecutionResult> findLatestExecutionsByTicketId(@Param("ticketId") String ticketId);
} 