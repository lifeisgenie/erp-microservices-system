package com.example.erp.approvalrequest.config;

import com.example.erp.approvalrequest.grpc.ApprovalRequestGrpcService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    private final ApprovalRequestGrpcService approvalRequestGrpcService;

    @Value("${grpc.server.port:9091}")
    private int port;

    private Server server;

    @PostConstruct
    public void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(approvalRequestGrpcService)
                .build()
                .start();

        System.out.println(">>> [ApprovalRequest] gRPC server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}