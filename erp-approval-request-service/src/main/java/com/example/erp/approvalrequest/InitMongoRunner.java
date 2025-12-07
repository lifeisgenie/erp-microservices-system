package com.example.erp.approvalrequest;

import com.example.erp.approvalrequest.domain.ApprovalDocument;
import com.example.erp.approvalrequest.domain.ApprovalDocument.Step;
import com.example.erp.approvalrequest.domain.ApprovalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
public class InitMongoRunner {

    @Bean
    public CommandLineRunner mongoTestRunner(ApprovalRepository approvalRepository) {
        return args -> {
            long count = approvalRepository.count();
            if (count == 0) {
                ApprovalDocument doc = new ApprovalDocument();

                // 간단한 더미 값만 채워서 저장
                // (나중에 진짜 로직 구현할 때 이 클래스는 지워도 됨)
                Step step1 = new Step();
                // step1.setStep(1);  // 세터 추가하면 값 넣어도 되고, 지금은 null이어도 상관 없음

                approvalRepository.save(doc);
                System.out.println(">>> Inserted test approval document (count was 0)");
            } else {
                System.out.println(">>> Approvals collection already has " + count + " documents");
            }
        };
    }
}