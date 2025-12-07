package com.example.erp.approvalrequest.config;

import approval.ApprovalGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcApprovalClientConfig implements DisposableBean {

    @Value("${approval.processing.grpc.host}")
    private String host;

    @Value("${approval.processing.grpc.port}")
    private int port;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel approvalManagedChannel() {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext() // TLS 없다고 가정
                .build();
        return this.channel;
    }

    @Bean
    public ApprovalGrpc.ApprovalBlockingStub approvalBlockingStub(ManagedChannel approvalManagedChannel) {
        return ApprovalGrpc.newBlockingStub(approvalManagedChannel);
    }

    @Override
    public void destroy() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}