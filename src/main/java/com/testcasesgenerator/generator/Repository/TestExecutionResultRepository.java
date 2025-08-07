package com.testcasesgenerator.generator.Repository;

import com.testcasesgenerator.generator.Model.TestExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionResultRepository extends JpaRepository<TestExecutionResult, Long> {
    List<TestExecutionResult> findByTicketIdAndExecutionStatusOrderByStartTimeDesc(String ticketId, String status);
    List<TestExecutionResult> findByTicketIdOrderByStartTimeDesc(String ticketId);
    List<TestExecutionResult> findByExecutionStatusOrderByStartTimeDesc(String status);
    List<TestExecutionResult> findAllByOrderByStartTimeDesc();
} 