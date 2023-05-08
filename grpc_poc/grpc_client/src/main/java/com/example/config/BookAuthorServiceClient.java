package com.example.config;

import com.example.BookAuthorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookAuthorServiceClient {
    private ManagedChannel getManagedChannel() {
        return ManagedChannelBuilder
                .forAddress("localhost", 9000)
                .usePlaintext()
                .build();
    }

    @Bean
    public BookAuthorServiceGrpc.BookAuthorServiceStub getAsyncClient() {
        return BookAuthorServiceGrpc.newStub(getManagedChannel());
    }

    @Bean
    public BookAuthorServiceGrpc.BookAuthorServiceBlockingStub getBlockingClient() {
        return BookAuthorServiceGrpc.newBlockingStub(getManagedChannel());
    }
}
