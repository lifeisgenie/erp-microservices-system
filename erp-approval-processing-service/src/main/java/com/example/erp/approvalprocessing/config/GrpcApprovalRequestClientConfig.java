package com.example.erp.approvalprocessing.config;

import approval.ApprovalGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcApprovalRequestClientConfig implements DisposableBean {

    @Value("${approval.request.grpc.host}")
    private String host;

    @Value("${approval.request.grpc.port}")
    private int port;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel approvalRequestManagedChannel() {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        return this.channel;
    }

    @Bean
    public ApprovalGrpc.ApprovalBlockingStub approvalRequestBlockingStub(ManagedChannel approvalRequestManagedChannel) {
        return ApprovalGrpc.newBlockingStub(approvalRequestManagedChannel);
    }

    @Override
    public void destroy() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}