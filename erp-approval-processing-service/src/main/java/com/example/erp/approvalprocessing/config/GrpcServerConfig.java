package com.example.erp.approvalprocessing.config;

import com.example.erp.approvalprocessing.grpc.ApprovalProcessingGrpcService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    private final ApprovalProcessingGrpcService approvalProcessingGrpcService;

    @Value("${grpc.server.port:9090}")
    private int port;

    private Server server;

    @PostConstruct
    public void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(approvalProcessingGrpcService)
                .build()
                .start();

        System.out.println(">>> gRPC server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}