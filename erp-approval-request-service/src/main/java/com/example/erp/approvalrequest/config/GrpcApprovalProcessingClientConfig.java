package com.example.erp.approvalrequest.config;

import approval.ApprovalGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcApprovalProcessingClientConfig implements DisposableBean {

    @Value("${approval.processing.grpc.host}")
    private String host;

    @Value("${approval.processing.grpc.port}")
    private int port;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel approvalProcessingManagedChannel() {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        return this.channel;
    }

    @Bean
    public ApprovalGrpc.ApprovalBlockingStub approvalProcessingBlockingStub(
            ManagedChannel approvalProcessingManagedChannel
    ) {
        return ApprovalGrpc.newBlockingStub(approvalProcessingManagedChannel);
    }

    @Override
    public void destroy() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}