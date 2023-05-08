package com.example.config;

import com.example.BookAuthorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class BookAuthorServiceClient {
    @Value("book-author-service.address")
    String address;


    private ManagedChannel getManagedChannel() {
        return ManagedChannelBuilder
                .forAddress(address, 9000)
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
