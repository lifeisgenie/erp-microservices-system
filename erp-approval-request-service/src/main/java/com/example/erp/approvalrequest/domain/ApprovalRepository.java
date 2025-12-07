package com.example.erp.approvalrequest.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ApprovalRepository extends MongoRepository<ApprovalDocument, String> {

    Optional<ApprovalDocument> findByRequestId(Integer requestId);
}