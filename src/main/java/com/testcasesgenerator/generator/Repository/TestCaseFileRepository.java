package com.testcasesgenerator.generator.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.testcasesgenerator.generator.Model.TestCaseFile;

import java.util.Optional;

@Repository
public interface TestCaseFileRepository extends JpaRepository<TestCaseFile, Long> {
    Optional<TestCaseFile> findByTicketId(String ticketId);
}
