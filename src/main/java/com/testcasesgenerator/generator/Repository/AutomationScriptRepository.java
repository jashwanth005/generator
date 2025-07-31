package com.testcasesgenerator.generator.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.testcasesgenerator.generator.Model.AutomationScript;

@Repository
public interface AutomationScriptRepository extends JpaRepository<AutomationScript, Long> {
    
    List<AutomationScript> findByTicketId(String ticketId);
    
    List<AutomationScript> findByTicketIdAndStatus(String ticketId, String status);
    
    Optional<AutomationScript> findByTestCaseId(String testCaseId);
    
    List<AutomationScript> findByScriptLanguage(String scriptLanguage);
    
    List<AutomationScript> findByScriptType(String scriptType);
    
    boolean existsByTicketIdAndTestCaseId(String ticketId, String testCaseId);
} 