package com.teamexp.learnflowapi.admin.repository;

import com.teamexp.learnflowapi.admin.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}
